package expose.model.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import expose.model.meta.UniversalModel;


public class XMLStore {

    private final ArrayList<UniversalModel> store = new ArrayList<>();
    public ArrayList<UniversalModel> getModels() { return store; }


    final int rootId = 100;

    public String getRootId() {
        return "100";
    }//getRootId
    public String getNewId() {

        if (size() < 1) {
            return getRootId();
        }

        int found = 1;

        for (UniversalModel model : store) {
            int id = Integer.parseInt(model.getId());
            int d = id - rootId;

            if (d >= found) {
                found = d + 1;
            }
        }//models


        return Integer.toString(rootId + found);
    }//getNewId


    public int size() {
        return store.size();
    }//store size
    public void close() {
        store.clear();
    }//store clear


    public UniversalModel getModelAt(int p) {
        if (p < 0) return null;

        if (p > size() - 1) return null;

        return store.get(p);
    }//getModel

    public UniversalModel getElderSibling(UniversalModel model) {
        UniversalModel found = null;

        UniversalModel source = findSource(model.getId());

        if (source != null) {
            String[] targets = source.getTargets().split(",");


            int l = targets.length;
            for (int p=0; p<l; p++) {
                String target_id = targets[p];

                if (target_id.equals(model.getId())) {
                    int q = p - 1;

                    if (q > -1) {
                        String sibling_id = targets[q];
                        found = findModel(sibling_id);
                    }
                }
            }

        }

        return found;
    }


    public UniversalModel getYoungerSibling(UniversalModel model) {
        UniversalModel found = null;

        UniversalModel source = findSource(model.getId());

        if (source != null) {
            String[] targets = source.getTargets().split(",");


            int l = targets.length;
            for (int p=0; p<l; p++) {
                String target_id = targets[p];

                if (target_id.equals(model.getId())) {
                    int q = p + 1;

                    if (q < l) {
                        String sibling_id = targets[q];
                        found = findModel(sibling_id);
                    }
                }
            }

        }

        return found;
    }


    /* --------------------------------windvolt-------------------------------- */

    public void swapModel(UniversalModel s, UniversalModel t) {


        String title = s.getTitle();
        String subject = s.getSubject();

        String type = s.getType();
        String date = s.getDate();
        String state = s.getState();
        String symbol = s.getSymbol();

        String content = s.getContent();
        String targets = s.getTargets();
        String tags = s.getTags();
        String specs = s.getSpecs();

        String coordinates = s.getCoordinates();
        String location = s.getLocation();


        s.setTitle(t.getTitle());
        s.setSubject(t.getSubject());

        s.setType(t.getType());
        s.setDate(t.getDate());
        s.setState(t.getState());
        s.setSymbol(t.getSymbol());

        s.setContent(t.getContent());
        s.setTargets(t.getTargets());
        s.setTags(t.getTags());

        s.setSpecs(t.getSpecs());

        s.setCoordinates(t.getCoordinates());
        s.setLocation(t.getLocation());


        t.setTitle(title);
        t.setSubject(subject);

        t.setType(type);
        t.setDate(date);
        t.setState(state);
        t.setSymbol(symbol);

        t.setContent(content);
        t.setTargets(targets);
        t.setTags(tags);

        t.setSpecs(specs);

        t.setCoordinates(coordinates);
        t.setLocation(location);
    }

    public void addModel(UniversalModel model) {
        store.add(model);
    }//addModel


    public boolean removeModelPosition(int position) {

        if (position < 0) {
            return false;
        }
        if (position > size()) {
            return false;
        }


        UniversalModel model = getModelAt(position);

        removeModel(model.getId());

        return true;
    }//removeModelPosition


    public boolean removeModel(String id) {
        UniversalModel model = findModel(id);

        if (model == null) {
            return false;
        }

        removeTree(model);
        store.remove(model);

        removeReferences(id);

        return true;
    }//removeModel


    private void removeReferences(String id) {
        if (id.isEmpty()) {
            return;
        }

        for (UniversalModel model : store) {

            String t = model.getTargets();
            String[] targets = t.split(",");

            t = "";
            for (String target : targets) {
                if (!target.isEmpty()) {
                    if (!target.equals(id)) {

                        t = appendId(t, target);
                    }
                }
            }

            model.setTargets(t);
        }
    }//removeReferences

    private void removeTree(UniversalModel source) {
        String targets = source.getTargets();
        String[] alltargets = targets.split(",");

        for (String target : alltargets) {
            UniversalModel model = findModel(target);
            if (model != null) {
                removeTree(model);

                removeReferences(target);
                store.remove(model);
            }//model
        }//target
    }//removeTree


    /* --------------------------------windvolt-------------------------------- */

    public UniversalModel findModel(String id) {
        if (id == null) {
            return null;
        }
        if (id.isEmpty()) {
            return null;
        }

        UniversalModel found = null;

        for (UniversalModel model : store) {
            String m_id = model.getId();

            if (m_id.equals(id)) {

                if (found == null) { // first hit
                    found = model;
                } // else model id is not unique
            }
        }//for

        return found;
    }//findModel


    public UniversalModel findSource(String target_id) {

        if (target_id == null) {
            return null;
        }
        if (target_id.isEmpty()) {
            return null;
        }

        UniversalModel source = null;

        for (UniversalModel model : store) {
            String targets = model.getTargets();

            String[] alltargets = targets.split(",");

            for (String id : alltargets) {

                if (id.equals(target_id)) {

                    if (source == null) { // first hit
                        source = model;

                    } else { // detect multiple parents error here
                        source = model;

                        //multiple sources error
                    }
                }

            }//target

        }//for

        return source;
    }//findSource


    public boolean findContent(String content) {
        boolean found = false;

        for (UniversalModel model : store) {

            if (content.equals(model.getSymbol())) {

                if (!found) { // return first hit

                    found = true;

                } // else content is not unique
            }
        }
        return found;
    }

    /* --------------------------------windvolt-------------------------------- */


    public void makeTarget(UniversalModel source, UniversalModel target) {

        if (source != null) {
            String targets = source.getTargets();

            targets = appendId(targets, target.getId());

            source.setTargets(targets);
        }


    }//makeTarget


    public int getTargetCount(UniversalModel source) {
        String targets = source.getTargets();
        String[] alltargets = targets.split(",");

        if (alltargets[0].equals("")) {
            return 0;
        }

        return alltargets.length;
    }//getTargetCount


    /* --------------------------------windvolt-------------------------------- */

    public UniversalModel createDefaultModel(String title, String subject) {
        UniversalModel model = new DiagramModel();



        model.setId(getNewId());
        model.setDate(today());

        model.setType("0");
        model.setState("0");

        model.setSymbol("");
        model.setTitle(title);
        model.setSubject(subject);
        model.setContent("");
        model.setTargets("");
        model.setTags("");

        model.setSpecs("");

        store.add(model);

        return model;
    }//createDefaultModel




    /* --------------------------------windvolt-------------------------------- */

    public String today() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        return dateFormat.format(date);
    }


    private String appendId(String id, String addendum) {

        if (id.isEmpty()) {
            return addendum;
        } else {
            return id + "," + addendum;
        }
    }//appendId
}
