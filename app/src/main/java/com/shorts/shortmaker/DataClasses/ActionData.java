package com.shorts.shortmaker.DataClasses;

import com.google.firebase.firestore.Exclude;
import com.shorts.shortmaker.ActionFactory;

import java.util.ArrayList;

public class ActionData {

    private String title;
    private String description;
    private ArrayList<String> data;
    private ActionFactory.Conditions condition;
    /* True by default */
    private Boolean isActivated;


    public ActionData() {
    }

    // not a real copy constructor
    @Exclude
    public void copyAction(ActionData actionData) {
        this.title = actionData.title;
        this.copyData(actionData.data);
        this.isActivated = actionData.isActivated;
        this.description = actionData.description;
        this.condition = actionData.condition;
    }

    public ActionData(String title) {
        this.title = title;
        isActivated = true;
        allocData();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getData() {
        return data;
    }

    public void copyData(ArrayList<String> data) {
        allocData();
        this.data.clear();
        this.data.addAll(data);
        this.condition = ActionFactory.Conditions.ON_DEFAULT;
    }

    @Exclude
    private void allocData() {
        if (this.data == null) {
            this.data = new ArrayList<>();
        }
    }

    public void setData(ArrayList<String> data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public Boolean getIsActivated() {
        return isActivated;
    }

    public void setIsActivated(Boolean activated) {
        isActivated = activated;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ActionFactory.Conditions getCondition() {
        return condition;
    }

    public void setCondition(ActionFactory.Conditions condition) {
        this.condition = condition;
    }
}
