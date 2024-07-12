/*
    This file is part of windvolt.

    created 2020 by Max Sumer
*/
package expose.model.meta;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public interface Cell {

    /**
     * cell model
     * @return @NonNull model
     */
    UniversalModel getModel();

    ImageView getImage();

    TextView getTitle();
    TextView getSubject();

    /**
     * middlepoint x position
     * @return x position
     */
    int x();
    void set_x(int value);

    /**
     * middlepoint y position
     * @return y position
     */
    int y();
    void set_y(int value);

    /**
     * set middelpoint xy
     */
    void set_xy(int x_value, int y_value);


    int iw();
    void setIw(int value);


    /**
     * cell width
     * @return width
     */
    int w();
    void set_w(int value);

    /**
     * cell hight
     * @return height
     */
    int h();
    void set_h(int value);

    /**
     * set dimension w/h
     */
    void set_wh(int w_value, int h_value);


    /**
     * redraw title, subject, image
     */
    void redraw();

    /**
     * mark cell with selection frame
     */
    void setSelected(boolean value);



    void setTextSize(int title_size, int subject_size);
    void setTextColor(int title_color, int subject_color);


    /**
     * indicates if targets are shown/hidden
     * @return 0: targets hidden, 1: targets shown
     */
    int getTargetState();
    void setTargetState(int value);




    View getView();
    void setLayoutParams(RelativeLayout.LayoutParams params);
}
