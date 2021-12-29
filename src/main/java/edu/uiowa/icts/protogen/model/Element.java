/*
 * Created on May 12, 2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.uiowa.icts.protogen.model;

import java.util.StringTokenizer;

public class Element {

    public String sqlLabel = null;
    public String label = null;
    String remarks = null;
    String description = null;
    protected String uid = null;
    protected String lowerLabel = null;
    protected String upperLabel = null;

    public Element() {
        super();
    }

    public String getLabel() {
        return label;
    }

    public String getUnqualifiedLabel() {
        return label.replace('.', '_');
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSqlLabel() {
        return sqlLabel;
    }

    public void setSqlLabel(String sqlLabel) {
        this.sqlLabel = sqlLabel;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
    
    public String getLowerLabel() {
        if (lowerLabel == null) {
            if (label.equals("ID"))
                lowerLabel = "id";
            else
                lowerLabel = Character.toLowerCase(label.charAt(0)) + label.substring(1);
        }
        
        return lowerLabel;
    }
    
    public String getUnqualifiedLowerLabel() {
        return getLowerLabel().replace('.', '_');
    }
    
    public String getUpperLabel() {
        if (upperLabel == null) {
            upperLabel = Character.toUpperCase(label.charAt(0)) + label.substring(1);
        }
        
        return upperLabel;
    }
    
    public void relabel(boolean leadingCapital) {
        sqlLabel = label;
        StringBuffer result = new StringBuffer();
        StringTokenizer theTokenizer = new StringTokenizer(label, "_-");
        while(theTokenizer.hasMoreTokens()) {
            String theToken = theTokenizer.nextToken().toLowerCase();
            result.append(Character.toUpperCase(theToken.charAt(0)));
            result.append(theToken.substring(1));
        }
        if (!leadingCapital)
            result.setCharAt(0, Character.toLowerCase(result.charAt(0)));
        label = result.toString();
    }
    
    public void relabelFull(boolean leadingCapital) {
        sqlLabel = label;
        StringBuffer result = new StringBuffer();
        StringTokenizer theTokenizer = new StringTokenizer(label, "_-");
        while(theTokenizer.hasMoreTokens()) {
            String theToken = theTokenizer.nextToken().toLowerCase();
            result.append(Character.toUpperCase(theToken.charAt(0)));
            result.append(theToken.substring(1));
        }
        if (!leadingCapital)
            result.setCharAt(0, Character.toLowerCase(result.charAt(0)));
        label = result.toString();
    }

    public String toString() {
        return getLabel();
    }

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}