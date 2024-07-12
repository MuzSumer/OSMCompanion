/*
    This file is part of windvolt.

    created 2020 by Max Sumer
*/
package expose.model.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ImageView;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import osm.expose.R;
import expose.model.meta.Diagram;
import expose.model.meta.Store;
import expose.model.meta.UniversalModel;


public class DiagramStore extends XMLStore implements Store {

    static String diagram_version = "*";



    private String namespace;


    public String getNamespace() {
        return namespace;
    }
    public void setNamespace(String value) { namespace = value; }




    public DiagramStore(Diagram diagram, String set_namespace) {
        namespace = set_namespace;
        if (namespace.isEmpty()) {
            namespace = "temp.mxl";
        }

        close();

        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(diagram.getContext());

    }//DiagramStore

    /* --------------------------------windvolt-------------------------------- */





    public boolean loadLocalModel(Diagram diagram, String folder) {
        FileInputStream fileInputStream;

        try {

            if (folder != null && !folder.isEmpty()) {

                File home = diagram.getContext().getFilesDir();
                File path = new File(home, folder);

                if (path.exists()) {
                    File input = new File(path, getNamespace());
                    fileInputStream = new FileInputStream(input);

                    buildContent(fileInputStream);
                    fileInputStream.close();

                    return true;
                }
            }

            fileInputStream = diagram.getContext().openFileInput(getNamespace());

            buildContent(fileInputStream);
            fileInputStream.close();

            return true;

            } catch (Exception e) {
                e.printStackTrace();
            }

        return false;
    }

    public boolean saveLocalModel(Diagram diagram, String folder) {
        FileOutputStream fileOutputStream;


        try {

            if (folder != null && !folder.isEmpty()) {

                File dir = diagram.getContext().getFilesDir();
                File fdir = new File(dir, folder);

                if (!fdir.exists()) {
                    fdir.mkdirs();
                }

                File output = new File(fdir, getNamespace());
                fileOutputStream = new FileOutputStream(output);

                transformFile(fileOutputStream);

                return true;
            }


            fileOutputStream = diagram.getContext().openFileOutput(getNamespace(), Context.MODE_PRIVATE);

            transformFile(fileOutputStream);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    private void transformFile(FileOutputStream fileOutputStream) {

        build = buildDocument();

        if (build == null) {
            return;
        }


        try {
            // save
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            //do not!
            //!transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            //!transformerFactory.setAttribute("indent-number", 1);

            //transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            //transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
            transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-10646"); // UCS Universal Character Set

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");

            DOMSource source = new DOMSource(build);
            StreamResult result = new StreamResult(fileOutputStream);
            transformer.transform(source, result);

            fileOutputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /* --------------------------------windvolt-------------------------------- */


    public boolean loadRemoteModel(Diagram diagram,
                                   String url) {

        new ModelLoader(diagram).execute(url);

        return true;
    }


    public boolean saveRemoteModel(Diagram diagram,
                                   String hostname, String username, String password,
                                   String url) {
        HttpURLConnection connection = null;

        build = buildDocument();

        if (build == null) {
            return false;
        }


        try {
            URL uri = new URL(url);
            connection = (HttpURLConnection) uri.openConnection();

            // login
            //String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            final String credentials = username + ":" + password;
            final byte[] credentialBytes = credentials.getBytes(); //ISO_8859_1

            byte[] encodedCredentials = Base64.encode(credentialBytes, Base64.DEFAULT);
            String authenticator = "Basic " + new String(encodedCredentials);


/*

            final byte[] credentialbytes = credentials.getBytes(ISO_8859_1);
            byte[] credeantialbytes = java.net.base64.encode(credentials.getBytes());

            final Base64.Encoder base64 = Base64.getEncoder();
            String authenticator = "Basic " + base64.encodeToString(credeantialbytes);
*/

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            connection.setRequestProperty("Authorization", authenticator);
            //connection.connect();

            OutputStream output = connection.getOutputStream();

            // save
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            //transformerFactory.setAttribute("indent-number", 1);
            Transformer transformer = transformerFactory.newTransformer();


            //transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");


            DOMSource source = new DOMSource(build);
            ByteArrayOutputStream out_bytes = new ByteArrayOutputStream();

            StreamResult result = new StreamResult(out_bytes);
            transformer.transform(source, result);

            output.write(out_bytes.toByteArray());

            output.flush();
            output.close();

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                connection.disconnect();

                return true;
            }


        } catch (Exception e) {

            e.printStackTrace();
        }

        if (connection != null) {
            connection.disconnect();
        }

        return false;
    }




    public boolean importModel(Diagram diagram, String url) {

        boolean success = loadRemoteModel(diagram, url);

        cooldown();

        if (success) {
            success = saveLocalModel(diagram, null);
        }

        return success;
    }


    public boolean pushFile(File file,
                                String hostname, String username, String password,
                                String folder, String filename) {
        try {

            /*
            JSch j = new JSch();

            Session session = j.getSession(username, hostname, 22);
            session.setPassword(password);

            Properties properties = new Properties();
            properties.put("StrictHostKeyChecking", "no");
            session.setConfig(properties);

            session.setConfig("PreferredAuthentications", "password");

            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftp = (ChannelSftp) channel;


            // folder
            {
                String home = sftp.pwd() + folder;
                SftpATTRS attributes = null;

                try {
                    attributes = sftp.stat(home);
                } catch (Exception e0) {
                    e0.printStackTrace();
                }
                if (attributes == null) {
                    sftp.mkdir(home);
                }

                sftp.cd(home);
            }


            // publish
            sftp.put(file.getAbsolutePath(), filename);

            sftp.disconnect();
            channel.disconnect();
            session.disconnect();

            cooldown();
             */


            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }





    /* --------------------------------windvolt-------------------------------- */




    public void loadViewImage(ImageView view, String url) {
        loadViewImage(view, url, -1, -1);
    }


    public void loadViewImage(ImageView view, String url, int w, int h) {

        new ImageLoader(view, w, h).execute(url);

    }


    private String concat0(String full) {
        return "0" + full;
    }
    public void loadLocalImage(Context context, ImageView image, String name) {

        String ii = name;
        while (ii.length() < 3) {
            ii = concat0(ii);
        }

        try {
            int id = context.getResources().getIdentifier("actn" + ii, "drawable", context.getPackageName());
            if (id != 0) {
                //Drawable d = r.getDrawable(id);
                //Drawable d = AppCompatResources.getDrawable(diagram.getContext(), id);
                //image.setImageDrawable(d);

                image.setImageResource(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }





    /* --------------------------------windvolt-------------------------------- */

    private static Document build;

    private void buildContent(InputStream stream) {


        try {
            // create builder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            if (stream instanceof FileInputStream) {

                // no conversion
                build = builder.parse(stream);


            } else { // online-diagram

                // conversion to byte array
                ByteArrayOutputStream output = new ByteArrayOutputStream();

                int b;
                while ((b = stream.read()) != -1) {
                    output.write(b);
                }

                ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

                // build and parse
                build = builder.parse(input);

            }

            stream.close();

            parseContent(build);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }//buildContent


    private void parseContent(Document document) {

        Element diagram = document.getDocumentElement();//diagram
        diagram.normalize();

        Attr version = diagram.getAttributeNode("version");
        if (version != null) {
            diagram_version = version.getNodeValue();
        }

        /*
        Attr node_author = diagram.getAttributeNode("author");
        if (node_author != null) {
            diagram_author = readItem(diagram, "author");
        }

        Attr node_time = diagram.getAttributeNode("time");
        if (node_time != null) {
            diagram_time = readItem(diagram, "author");
        }
         */



        NodeList models = diagram.getElementsByTagName("model");
        int size = models.getLength();

        for (int p=0; p<size; p++) {
            Node node = models.item(p);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) node;

                String id;

                Attr node_id = e.getAttributeNode(UniversalModel.N_ID);
                if (node_id != null) {
                    id = node_id.getNodeValue();
                } else {
                    id = readItem(e,"id");
                }

                String type = readItem(e, UniversalModel.N_TYPE);
                String date = readItem(e, UniversalModel.N_DATE);
                String state = readItem(e,UniversalModel.N_STATE);
                String symbol = readItem(e, UniversalModel.N_SYMBOL);

                String title = readItem(e, UniversalModel.N_TITLE);
                String subject = readItem(e, UniversalModel.N_SUBJECT);

                String content = readItem(e, UniversalModel.N_CONTENT);
                String targets = readItem(e, UniversalModel.N_TARGETS);
                String tags = readItem(e, UniversalModel.N_TAGS);

                String specs = readItem(e, UniversalModel.N_SPECS);

                String location = readItem(e, UniversalModel.N_LOCATION);
                String coordinates = readItem(e, UniversalModel.N_COORDINATES);



                UniversalModel model = new DiagramModel();

                model.setId(id);
                model.setType(type);
                model.setDate(date);
                model.setState(state);
                model.setSymbol(symbol);

                model.setTitle(title);
                model.setSubject(subject);

                model.setContent(content);

                model.setTargets(targets);
                model.setTags(tags);

                model.setSpecs(specs);

                model.setLocation(location);
                model.setCoordinates(coordinates);

                addModel(model);
            }//element

        }//node

    }//parseContent

    private String readItem(Element element, String name) {
        String result = "";

        NodeList node = element.getElementsByTagName(name);
        if (node != null) {
            try {
                result = node.item(0).getTextContent();
            } catch (Exception e) {
                //e.printStackTrace();
                //expected
            }
        }

        return result;
    }//readItem




    private Document buildDocument() {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            build = builder.newDocument();
            //build.setXmlStandalone(false);


            Element diagram = build.createElement("windvolt");
            diagram.setAttribute("version", diagram_version);

            /*
            diagram.setAttribute("author", diagram_author);

            Long time = System.currentTimeMillis();
            diagram.setAttribute("time", Long.toString(time));
             */

            build.appendChild(diagram);



            for (UniversalModel model : getModels()) {

                // create model
                Element node = build.createElement("model");
                node.setAttribute(UniversalModel.N_ID, model.getId());

                diagram.appendChild(node);

                //addElement(m,"id", model.getId());
                writeItem(node, UniversalModel.N_TYPE, model.getType());
                writeItem(node, UniversalModel.N_DATE, model.getDate());
                writeItem(node, UniversalModel.N_STATE, model.getState());
                writeItem(node, UniversalModel.N_SYMBOL, model.getSymbol());
                writeItem(node, UniversalModel.N_TITLE, model.getTitle());
                writeItem(node, UniversalModel.N_SUBJECT, model.getSubject());
                writeItem(node, UniversalModel.N_CONTENT, model.getContent());
                writeItem(node, UniversalModel.N_TARGETS, model.getTargets());
                writeItem(node, UniversalModel.N_TAGS, model.getTags());

                writeItem(node, UniversalModel.N_SPECS, model.getSpecs());
                writeItem(node, UniversalModel.N_LOCATION, model.getLocation());
                writeItem(node, UniversalModel.N_COORDINATES, model.getCoordinates());
            }

            return build;


        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }//buildDocument

    private void writeItem(Element parent, String name, String value) {

        Element element = build.createElement(name);
        element.appendChild(build.createTextNode(value));
        parent.appendChild(element);

    }


    /* --------------------------------windvolt-------------------------------- */


    private class ModelLoader extends AsyncTask<String, Void, Boolean> {

        HttpsURLConnection connection = null;
        InputStream contentstream = null;
        String url = null;

        Diagram diagram;

        public ModelLoader(Diagram set_diagram) {
            diagram = set_diagram;
        }

        @Override
        protected Boolean doInBackground(String... values) {
            url = values[0];


            try {

                URL uri = new URL(url);
                connection = (HttpsURLConnection) uri.openConnection();

                connection.setRequestMethod("GET");
                connection.setDoInput(true);


                connection.connect();

                if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {

                    contentstream = connection.getInputStream();
                    buildContent(contentstream);

                    return true;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            {
                if (contentstream != null) {
                    try {
                        contentstream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                    //cooldown();
                }
            }//cleanup

            if (result) {
                diagram.setFocus(null, false);
            }

        }//onPostExecute


    }//ModelLoader


    /* --------------------------------windvolt-------------------------------- */


    private static class ImageLoader extends AsyncTask<String, Void, Bitmap> {
        String url;
        HttpsURLConnection connection = null;
        InputStream content = null;
        private final ImageView view;
        int w, h;

        public ImageLoader(ImageView set_view, int set_w, int set_h) {
            view = set_view;
            w = set_w;
            h = set_h;
        }

        private boolean isToolImage(String exp) {

            //if (exp == null) return false;
            //if (exp.isEmpty()) return false;

            // exp.matches("[-+]?\\d*\\.?\\d+")

            try {
                int i = Integer.parseInt(exp);

                if (i < 1) return false;
                if (i > 154) return false;

                return true;
            } catch (Exception e) {
                return false;
            }
        }
        private String concat0(String full) {
            return "0" + full;
        }
        protected Bitmap doInBackground(String... values) {
            url = values[0];


            if (url.isEmpty()) { return null; }



            // numeric
            if (isToolImage(url)) {

                String numeric = url;
                while (numeric.length() < 3) {
                    numeric = concat0(numeric);
                }

                url = "https://windvolt.eu/model/icons/actn/actn" + numeric + ".gif";
            }



            Bitmap bitmap = null;

            try {

                String local = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();

                int l = url.length();
                String ext = url.substring(l-4, l);

                if (url.startsWith(local)) { // load local

                    switch (ext) {

                        // video
                        case ".3gp":
                        case ".mp4":
                            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(url, MediaStore.Images.Thumbnails.MINI_KIND);
                            bitmap = new BitmapDrawable(thumb).getBitmap();
                            break;

                        // image
                        case ".gif":
                        case ".jpg":
                            bitmap = BitmapFactory.decodeFile(url);
                            break;

                        default:
                            bitmap = BitmapFactory.decodeFile(url);
                            break;
                    }
                }

                else { // load remote

                    URL uri = new URL(url);
                    connection = (HttpsURLConnection) uri.openConnection();

                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);


                    connection.connect();


                    if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {

                        content = connection.getInputStream();


                        switch (ext) {

                            // video
                            case ".3gp":
                            case ".mp4":
                                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(url, MediaStore.Images.Thumbnails.MINI_KIND);
                                bitmap = new BitmapDrawable(thumb).getBitmap();
                                break;

                            // image
                            case ".gif":
                            case ".jpg":
                                bitmap = BitmapFactory.decodeStream(content);
                                break;

                            default:
                                bitmap = BitmapFactory.decodeStream(content);
                                break;
                        }


                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return bitmap;
        }


        protected void onPostExecute(Bitmap result) {
            {//*cleanup
                if (content != null) {
                    try {
                        content.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }//cleanup



            if (result == null) {


                view.setImageResource(R.drawable.ic_error);



            } else {


                if (w < 0) {
                    w = result.getWidth();
                }
                if (h < 0) {
                    h = result.getHeight();
                }

                Bitmap scaled = Bitmap.createScaledBitmap(result, w, h, false);
                view.setImageBitmap(scaled);
            }
        }


    }//ImageLoader



    private void cooldown() {
        SystemClock.sleep(1200);
    }

}
