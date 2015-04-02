
package com.krishagni.catissueplus.core.auth.services.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;

import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.auth.domain.AuthErrorCode;
import com.krishagni.catissueplus.core.auth.domain.AuthToken;
import com.krishagni.catissueplus.core.auth.domain.LoginAuditLog;
import com.krishagni.catissueplus.core.auth.events.LoginDetail;
import com.krishagni.catissueplus.core.auth.events.TokenDetail;
import com.krishagni.catissueplus.core.auth.services.AuthenticationService;
import com.krishagni.catissueplus.core.auth.services.UserAuthenticationService;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.PlusTransactional;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;
import com.krishagni.catissueplus.core.common.events.UserSummary;
import com.krishagni.catissueplus.core.common.util.Status;

public class UserAuthenticationServiceImpl implements UserAuthenticationService {
	private static final int LOGIN_FAILED_ATTEMPT = 5;

	private DaoFactory daoFactory;

	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	@Override
	@PlusTransactional
	public ResponseEvent<Map<String, Object>> authenticateUser(RequestEvent<LoginDetail> req) {
		LoginDetail loginDetail = req.getPayload();
		User user = null;
		try {
			user = daoFactory.getUserDao().getUser(loginDetail.getLoginId(), loginDetail.getDomainName());
			if (user == null) {
				user = daoFactory.getUserDao().getUserByEmailAddress(loginDetail.getLoginId());
			}
			
			if (user == null) {
				throw OpenSpecimenException.userError(AuthErrorCode.INVALID_CREDENTIALS);
			}
			
			if (user.getActivityStatus().equals(Status.ACTIVITY_STATUS_LOCKED.getStatus())) {
				throw OpenSpecimenException.userError(AuthErrorCode.USER_LOCKED);
			}
			
			if (!user.isEnabled()) {
				throw OpenSpecimenException.userError(AuthErrorCode.INVALID_CREDENTIALS);
			}
			
			AuthenticationService authService = user.getAuthDomain().getAuthProviderInstance();
			authService.authenticate(user.getLoginName(), loginDetail.getPassword());
			
			LoginAuditLog loginAuditLog = insertLoginAudit(user, loginDetail.getIpAddress(), true);
			Map<String, Object> authDetail = new HashMap<String, Object>();
			authDetail.put("user", user);
 
			if (!loginDetail.isDoNotGenerateToken()) {
				String token = UUID.randomUUID().toString();
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.HOUR, 8); // valid for 8 hrs

				AuthToken authToken = new AuthToken();
				authToken.setExpiresOn(cal.getTime());
				authToken.setIpAddress(loginDetail.getIpAddress());
				authToken.setToken(token);
				authToken.setUser(user);
				authToken.setLoginAuditLog(loginAuditLog);

				daoFactory.getAuthDao().saveAuthToken(authToken);

				String userToken = token + ":" + cal.getTime().getTime();
				authDetail.put("token", encodeToken(userToken));
			}
			
			return ResponseEvent.response(authDetail);
		} catch (OpenSpecimenException ose) {
			if (user != null && user.isEnabled()) {
				insertLoginAudit(user, loginDetail.getIpAddress(), false);
				checkFailedLoginAttempt(user, loginDetail.getIpAddress());
			}
			return ResponseEvent.error(ose, true);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	@PlusTransactional
	public ResponseEvent<User> validateToken(RequestEvent<TokenDetail> req) {
		try {
			TokenDetail tokenDetail = req.getPayload();
			String userToken = decodeToken(tokenDetail.getToken());
			String[] parts = userToken.split(":");
			if (parts.length != 2) {
				throw OpenSpecimenException.userError(AuthErrorCode.INVALID_TOKEN);
			}

			long expiresOn = Long.parseLong(parts[1]);
			if (expiresOn < System.currentTimeMillis()) {
				throw OpenSpecimenException.userError(AuthErrorCode.TOKEN_EXPIRED);
			}

			String token = parts[0];
			AuthToken authToken = daoFactory.getAuthDao().getAuthTokenByKey(token);
			if (authToken == null) {
				throw OpenSpecimenException.userError(AuthErrorCode.INVALID_TOKEN);
			}

			if (authToken.getExpiresOn().getTime() < System.currentTimeMillis()) {
				daoFactory.getAuthDao().deleteAuthToken(authToken);
				throw OpenSpecimenException.userError(AuthErrorCode.TOKEN_EXPIRED);
			}

			String ipAddress = tokenDetail.getIpAddress();
			if (!authToken.getIpAddress().equals(ipAddress)) {
				throw OpenSpecimenException.userError(AuthErrorCode.IP_ADDRESS_CHANGED);
			}

			User user = authToken.getUser();

			if (!Hibernate.isInitialized(user)) {
				Hibernate.initialize(user);
			}

			return ResponseEvent.response(user);
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	@PlusTransactional
	public ResponseEvent<UserSummary> getCurrentLoggedInUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = (User)auth.getPrincipal();
		return ResponseEvent.response(UserSummary.from(user));
	}

	@PlusTransactional
	public ResponseEvent<String> removeToken(RequestEvent<String> req) {
		String userToken = decodeToken(req.getPayload());
		String[] parts = userToken.split(":");
		try {
			AuthToken token = daoFactory.getAuthDao().getAuthTokenByKey(parts[0]);
			LoginAuditLog loginAuditLog = token.getLoginAuditLog();
			loginAuditLog.setLogoutTime(Calendar.getInstance().getTime());
			
			daoFactory.getAuthDao().deleteAuthToken(token);
			return ResponseEvent.response("Success");
		} catch (Exception e) {	
			return ResponseEvent.serverError(e);
		}
	}
	
	@Scheduled(cron="0 0 12 ? * *")
	@PlusTransactional
	public void deleteExpiredTokens() {
		daoFactory.getAuthDao().deleteExpiredAuthToken(Calendar.getInstance().getTime());
	}
	
	private LoginAuditLog insertLoginAudit(User user, String ipAddress, boolean loginSuccessful) {
		LoginAuditLog loginAuditLog = new LoginAuditLog();
		loginAuditLog.setUser(user);
		loginAuditLog.setIpAddress(ipAddress);
		loginAuditLog.setLoginTime(Calendar.getInstance().getTime());
		loginAuditLog.setLogoutTime(null);
		loginAuditLog.setLoginSuccessful(loginSuccessful); 
		
		daoFactory.getAuthDao().saveLoginAuditLog(loginAuditLog);
		return loginAuditLog;
	}
	
	private void checkFailedLoginAttempt(User user, String ipAddress) {
		List<LoginAuditLog> logs =
				daoFactory.getAuthDao().getLoginAuditLogsByUser(user.getId(), LOGIN_FAILED_ATTEMPT);
		if (logs.size() < LOGIN_FAILED_ATTEMPT) {
			return;
		}
		
		for (LoginAuditLog log: logs) {
			if (log.isLoginSuccessful()) {
				return;
			}
		}
		
		user.setActivityStatus(Status.ACTIVITY_STATUS_LOCKED.getStatus());
	}
	
	private String encodeToken(String token) {
		return new String(Base64.encode(token.getBytes()));
	}
	
	private String decodeToken(String token) {
		return new String(Base64.decode(token.getBytes()));
	}
	
}
