package redlaboratory.littlelaboratory;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by 지수 on 2016-09-02.
 */
public class Experiment implements Serializable {

    private String title;
    private String description;
    private Calendar addedDate;

    public Experiment(String title, String description, Calendar addedDate) {
        this.title = title;
        this.description = description;
        this.addedDate = addedDate;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Calendar getAddedDate() {
        return addedDate;
    }

}
