/*
Copyright ©2004, University of Toronto. All rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a 
copy of this software and associated documentation files (the "Software"), 
to deal in the Software without restriction, including without limitation 
the rights to use, copy, modify, merge, publish, distribute, sublicense, 
and/or sell copies of the Software, and to permit persons to whom the 
Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included 
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Adaptive Technology Resource Centre, University of Toronto
130 St. George St., Toronto, Ontario, Canada
Telephone: (416) 978-4360
*/

package ca.utoronto.atrc.tile.accessibilitychecker;

import es.inteco.common.logging.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuidelineGroup {
    String name;
    String principle;
    String nameId;
    String url;
    String number;
    List<Integer> checks;
    List<Integer> noExecutedMarkChecks;
    List<Integer> onlyWarningChecks;
    Map<Integer, Integer> relatedChecks;
    List<GuidelineGroup> groups;
    String type;
    String aspect;
    String level;

    public GuidelineGroup() {
        name = "";
        principle = "";
        nameId = "";
        url = "";
        number = "";
        checks = new ArrayList<Integer>();
        groups = new ArrayList<GuidelineGroup>();
        noExecutedMarkChecks = new ArrayList<Integer>();
        onlyWarningChecks = new ArrayList<Integer>();
        relatedChecks = new HashMap<Integer, Integer>();
        type = "";
        aspect = "";
        level = "";
    }

    public String getName() {
        return name;
    }

    public String getPrinciple() {
        return principle;
    }

    public String getNameId() {
        return nameId;
    }

    public String getNumber() {
        return number;
    }

    public String getUrl() {
        return url;
    }

    public List<Integer> getChecksVector() {
        return checks;
    }

    public List<Integer> getNoExecutedMarkChecks() {
        return noExecutedMarkChecks;
    }

    public List<Integer> getOnlyWarningChecks() {
        return onlyWarningChecks;
    }

    public Map<Integer, Integer> getRelatedChecks() {
        return relatedChecks;
    }

    public void setRelatedChecks(Map<Integer, Integer> relatedChecks) {
        this.relatedChecks = relatedChecks;
    }

    public List<GuidelineGroup> getGroupsVector() {
        return groups;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAspect() {
        return aspect;
    }

    public void setAspect(String aspect) {
        this.aspect = aspect;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getCount(int confidenceGiven) {
        int count = 0;

        AllChecks allChecks = EvaluatorUtility.getAllChecks();
        for (int x = 0; x < checks.size(); x++) {
            Integer checkId = (Integer) checks.get(x);

            Check check = allChecks.getCheck(checkId.intValue());
            if (check != null) {
                if (check.getConfidence() == confidenceGiven) {
                    count++;
                }
            }
        }

        for (int y = 0; y < groups.size(); y++) {
            GuidelineGroup group = (GuidelineGroup) groups.get(y);
            count += group.getCount(confidenceGiven);
        }

        return count;
    }

    public boolean initialize(Node node, Guideline guideline) {
        boolean foundName = false;

        type = ((Element) node).getAttribute("type");

        level = ((Element) node).getAttribute("level");

        aspect = ((Element) node).getAttribute("aspect");

        NodeList childNodes = node.getChildNodes();
        for (int x = 0; x < childNodes.getLength(); x++) {
            if (childNodes.item(x).getNodeName().compareToIgnoreCase("name") == 0) {
                if (foundName == true) {
                    Logger.putLog("Warning: group has more than one name!", GuidelineGroup.class, Logger.LOG_LEVEL_WARNING);
                    return false;
                } else {
                    // is this a WCAG guideline? (id will be non-null if it is WCAG)
                    String stringId = ((Element) childNodes.item(x)).getAttribute("id");
                    if ((stringId != null) && (stringId.length() > 0)) {
                        nameId = stringId; // nameId is not empty so we know this is WCAG
                        name = EvaluatorUtility.getElementText(childNodes.item(x));
                        principle = ((Element) childNodes.item(x)).getAttribute("principle");
                    } else { // regular guideline
                        name = EvaluatorUtility.getElementText(childNodes.item(x));
                    }
                    foundName = true;
                }
            } else if (childNodes.item(x).getNodeName().compareToIgnoreCase("checks") == 0) {
                guideline.initializeChecks(childNodes.item(x), checks, noExecutedMarkChecks, onlyWarningChecks, name, relatedChecks);
            } else if (childNodes.item(x).getNodeName().equalsIgnoreCase("subgroup")
                    || childNodes.item(x).getNodeName().equalsIgnoreCase("suitability")) {
                GuidelineGroup group = new GuidelineGroup();
                if (group.initialize(childNodes.item(x), guideline) == true) {
                    groups.add(group);
                }
            }

        }
        if (foundName == false) {
            Logger.putLog("Warning: group has no name!", GuidelineGroup.class, Logger.LOG_LEVEL_WARNING);
            return false;
        }

        return true;
    }

    public void getChecks(List inChecks) {
        // add the checks from this guideline
        for (int x = 0; x < checks.size(); x++) {
            inChecks.add((Integer) checks.get(x));
        }

        // add checks from all guideline groups
        for (int x = 0; x < groups.size(); x++) {
            GuidelineGroup group = (GuidelineGroup) groups.get(x);
            group.getChecks(inChecks);
        }
    }

    // Returns true if this group contains the given check.
    public boolean containsCheck(int checkId) {
        for (int x = 0; x < checks.size(); x++) {
            if (((Integer) checks.get(x)).intValue() == checkId) {
                return true;
            }
        }

        // try the subgroups
        for (int x = 0; x < groups.size(); x++) {
            GuidelineGroup group = (GuidelineGroup) groups.get(x);
            if (group.containsCheck(checkId) == true) {
                return true;
            }
        }
        return false;
    }

    // Returns the subgroup of a given check
    public String getSubgroupFromCheck(int checkId) {
        for (int x = 0; x < groups.size(); x++) {
            GuidelineGroup group = (GuidelineGroup) groups.get(x);
            if (group.containsCheck(checkId) == true) {
                return group.name;
            }
        }
        return "";
    }

}