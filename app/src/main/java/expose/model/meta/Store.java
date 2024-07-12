/*
    This file is part of windvolt.

    created 2020 by Max Sumer
*/
package expose.model.meta;

import android.content.Context;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

public interface Store {
    int size();

    /**
     * disposes all models
     */
    void close();


    ArrayList<UniversalModel> getModels();

    /**
     * root identification
     */
    String getRootId();

    /**
     * new identification
     */
    String getNewId();



    /**
     * namespace equals diagram url
     */
    String getNamespace();
    void setNamespace(String value);



    String today();


    /**
     * creates a default model by title and subject
     */
    UniversalModel createDefaultModel(String title, String subject);




    /**
     * adds a specified model
     */
    void addModel(UniversalModel model);

    /**
     * returns the model at position or null
     */
    UniversalModel getModelAt(int position);



    /**
     * removes a model by identification
     */
    boolean removeModel(String model_id);

    /**
     * removes model at position
     */
    boolean removeModelPosition(int position);




    /**
     * finds a model by identification
     */
    UniversalModel findModel(String model_id);

    /**
     * finds the first model that references the identified model
     */
    UniversalModel findSource(String target_id);


    /**
     * searches the store for equal content
     */
    boolean findContent(String content);



    /**
     * makes a model target to a source model
     */
    void makeTarget(UniversalModel source, UniversalModel target);

    /**
     * the number of targets
     */
    int getTargetCount(UniversalModel source);



    /**
     * finds the elder sibling
     */
    UniversalModel getElderSibling(UniversalModel model);

    /**
     * finds the younger sibling
     */
    UniversalModel getYoungerSibling(UniversalModel model);


    void swapModel(UniversalModel source, UniversalModel target);


    /**
     * loads a local model from /data
     */
    boolean loadLocalModel(Diagram diagram, String folder);

    /**
     * saves local model to /data
     */
    boolean saveLocalModel(Diagram diagram, String folder);



    /**
     * loads a remote model from a server and starts the diagram
     */
    boolean loadRemoteModel(Diagram diagram, String url);

    /**
     * deprecated: publish model instead
     * saves a xml model to a remote server
     */
    boolean saveRemoteModel(Diagram diagram, String hostname, String username, String password, String url);




    /**
     * imports a remote model to the diagrams store
     */
    boolean importModel(Diagram diagram, String url);


    /**
     * pushes a file to a remote server using SFTP
     */
    boolean pushFile(File file,
                     String hostname, String username, String password,
                     String filepath, String filename);





    /**
     * loads local and remote images
     */
    void loadViewImage(ImageView view, String url);

    /**
     * loads local and remote images and scales them
     */
    void loadViewImage(ImageView view, String url, int w, int h);

    /**
     * loads an image from app resources
     */
    void loadLocalImage(Context context, ImageView image, String name);

}
