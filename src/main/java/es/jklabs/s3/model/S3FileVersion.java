package es.jklabs.s3.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class S3FileVersion implements Serializable {
    private static final long serialVersionUID = 1300082330894333475L;
    private String id;
    private Date fecha;
    private S3File s3File;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        S3FileVersion that = (S3FileVersion) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public S3File getS3File() {
        return s3File;
    }

    public void setS3File(S3File s3File) {
        this.s3File = s3File;
    }
}
