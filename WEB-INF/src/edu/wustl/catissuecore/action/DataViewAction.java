/**
 * <p>Title: DataViewAction Class>
 * <p>Description:	DataViewAction is used to show the query results data 
 * in spreadsheet or individaul view.</p>
 * Copyright:    Copyright (c) year
 * Company: Washington University, School of Medicine, St. Louis.
 * @author Gautam Shetty
 * @version 1.00
 */
package edu.wustl.catissuecore.action;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.wustl.catissuecore.query.ResultData;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.util.logger.Logger;

/**
 * DataViewAction is used to show the query results data 
 * in spreadsheet or individaul view.
 * @author gautam_shetty
 */
public class DataViewAction extends BaseAction
{
    
    /**
     * Overrides the execute method in Action class.
     */
    public ActionForward executeAction(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        String target = Constants.SUCCESS;
    	String nodeName = request.getParameter("nodeName");
    	Logger.out.debug("nodename of selected node"+nodeName);
        StringTokenizer str = new StringTokenizer(nodeName,":");
        String name = str.nextToken().trim();
        HttpSession session = request.getSession();
        String id = new String();
        
        if (!name.equals(Constants.ROOT))
        {
        	id = str.nextToken();
        }

        //get the type of view to show (spreadsheet/individual)
        String viewType = request.getParameter(Constants.VIEW_TYPE);
        
        if (viewType.equals(Constants.SPREADSHEET_VIEW))
        {
            if (!name.equals(Constants.ROOT))
            {
                Map columnIdsMap = (Map)session.getAttribute(Constants.COLUMN_ID_MAP);
                Logger.out.debug("alias name of selected node in adv tree:"+name);
                Logger.out.debug("column ids map in data view action"+columnIdsMap);
                String key = name+"."+Constants.IDENTIFIER;
                int columnId = ((Integer)columnIdsMap.get(name+"."+Constants.IDENTIFIER)).intValue()-1;
                Logger.out.debug("columnid of selected node:"+columnId+"in the map for key:"+key);
                name=Constants.COLUMN+columnId;
                Logger.out.debug("Column name of the selected column in the tree:"+name);
            }
        	List list = null;
            String[] columnList= null;
            //String[] columnList = {"Participant1_IDENTIFIER","CollectionProtocol1_IDENTIFIER","SpecimenCollectionGroup1_IDENTIFIER","Specimen1_IDENTIFIER"};
            

            ResultData resultData = new ResultData();
            
            //columnList = (String[]) session.getAttribute(Constants.SELECT_COLUMN_LIST);
            
            /*if (columnList == null)
            {
                columnList = Constants.DEFAULT_SPREADSHEET_COLUMNS;
            }*/
            
            list = resultData.getSpreadsheetViewData(name,id,columnList, getSessionData(request), Constants.OBJECT_LEVEL_SECURE_RETRIEVE);
            Logger.out.debug("list of data after advance search"+list);
     		// If the result contains no data, show error message.
    		if (list.isEmpty()) 
    		{
    			Logger.out.debug("inside if condition for empty list");
    			ActionErrors errors = new ActionErrors();
    			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("advanceQuery.noRecordsFound"));
    			saveErrors(request, errors);
    			//target = Constants.FAILURE;
    		}
    		else
    		{
            
    			//Get column display names from session which is set in session in AdvanceSearchResultsAction
            	List columnDisplayNames = (List)session.getAttribute(Constants.COLUMN_DISPLAY_NAMES);
    			
            	//Created temporary column display names for the spreadsheet view
            	/*List rowList = (List)list.get(0);
    			int columnSize=rowList.size();
    			for(int i=0;i<columnSize;i++)
    				columnDisplayNames.add("Column"+i);
            	Map columnIdsMap = (Map)session.getAttribute(Constants.COLUMN_ID_MAP);
            	Set keySet = columnIdsMap.keySet();
            	//Split the keys which is in the form of aliasName.columnNames to get the column Names 
            	Iterator keySetitr = keySet.iterator();
            	List columnDisplayNames = new ArrayList(keySet);*/
    			
    			request.setAttribute(Constants.SPREADSHEET_COLUMN_LIST,columnDisplayNames);
    			request.setAttribute(Constants.SPREADSHEET_DATA_LIST,list);
    			request.setAttribute(Constants.PAGEOF,Constants.PAGEOF_QUERY_RESULTS);
    		}
        }
        
        else
        {
            String url = null;
            Logger.out.debug("selected node name in object view:"+name+"object");
            if (name.equals(Constants.PARTICIPANT))
            {
            	Logger.out.debug("indside participant object view");
                url = new String(Constants.QUERY_PARTICIPANT_SEARCH_ACTION+id+"&"+Constants.PAGEOF+"="+
                														Constants.PAGEOF_PARTICIPANT_QUERY);
            }
            else
            {
                if (name.equals(Constants.ACCESSION))
                {
                    url = new String(Constants.QUERY_ACCESSION_SEARCH_ACTION+id);
                }
            }
            Logger.out.debug("url of object view:"+url);
            RequestDispatcher requestDispatcher = request.getRequestDispatcher(url);
            requestDispatcher.forward(request,response);
            
        }
        
        return mapping.findForward(target);
    }

}
