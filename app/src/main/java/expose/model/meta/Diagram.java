/*
    This file is part of windvolt.

    created 2020 by Max Sumer
*/
package expose.model.meta;

import android.content.Context;

public interface Diagram {

    Context getContext();
    Store getStore();

    /**
     * create the store
     * you have to implement this method
     *
     * sample:
     * public void createStore() {
     *     String namespace = "temp.xml";
     *
     *     setStore(new DiagramStore(namespace);
     *     getStore().loadLocalModel(getContext());
     *
     *     setFocus(null, false);
     * }
     */
    boolean createStore(Store store, String namespace, String folder);



    String getFolder();
    void setFolder(String value);




    /**
     * sets the diagram focus
     * @param id, the identification of the model to be focused
     * @param expand, true to expand model targets
     */
    void setFocus(String id, boolean expand);


    void redraw(boolean full_redraw);



    void scrollToEnd();
    void scrollToTop();
    void scrollBy(int x, int y);



    String getSelected();
    void setSelected(String id);

    /**
     * the root model of the diagram
     * @return root model or null
     */
    UniversalModel getRootModel();

    /**
     * the focused model of the diagram
     * @return focused model or null
     */
    UniversalModel getSelectedModel();


    Cell findCell(UniversalModel model);
    Cell addCell(UniversalModel model);
    void removeCell(UniversalModel model);

}
