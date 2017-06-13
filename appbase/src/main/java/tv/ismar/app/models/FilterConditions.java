package tv.ismar.app.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by admin on 2017/5/27.
 */

public class FilterConditions {

    @SerializedName("default")
    private String defaultX;
    private AttributesBean attributes;
    private String content_model;

    public String getDefaultX() {
        return defaultX;
    }

    public void setDefaultX(String defaultX) {
        this.defaultX = defaultX;
    }

    public AttributesBean getAttributes() {
        return attributes;
    }

    public void setAttributes(AttributesBean attributes) {
        this.attributes = attributes;
    }

    public String getContent_model() {
        return content_model;
    }

    public void setContent_model(String content_model) {
        this.content_model = content_model;
    }

    public static class AttributesBean {

        private GenreBean genre;
        private AreaBean area;
        private AirDateBean air_date;
        private AgeBean age;
        private FeatureBean feature;

        public FeatureBean getFeature() {
            return feature;
        }

        public void setFeature(FeatureBean feature) {
            this.feature = feature;
        }

        public AgeBean getAge() {
            return age;
        }

        public void setAge(AgeBean age) {
            this.age = age;
        }



        public GenreBean getGenre() {
            return genre;
        }

        public void setGenre(GenreBean genre) {
            this.genre = genre;
        }

        public AreaBean getArea() {
            return area;
        }

        public void setArea(AreaBean area) {
            this.area = area;
        }

        public AirDateBean getAir_date() {
            return air_date;
        }

        public void setAir_date(AirDateBean air_date) {
            this.air_date = air_date;
        }

        public static class GenreBean {


            private int position;
            private String label;
            private List<List<String>> values;

            public int getPosition() {
                return position;
            }

            public void setPosition(int position) {
                this.position = position;
            }

            public String getLabel() {
                return label;
            }

            public void setLabel(String label) {
                this.label = label;
            }

            public List<List<String>> getValues() {
                return values;
            }

            public void setValues(List<List<String>> values) {
                this.values = values;
            }
        }

        public static class AreaBean {


            private int position;
            private String label;
            private List<List<String>> values;

            public int getPosition() {
                return position;
            }

            public void setPosition(int position) {
                this.position = position;
            }

            public String getLabel() {
                return label;
            }

            public void setLabel(String label) {
                this.label = label;
            }

            public List<List<String>> getValues() {
                return values;
            }

            public void setValues(List<List<String>> values) {
                this.values = values;
            }
        }

        public static class AirDateBean {


            private int position;
            private String label;
            private List<List<String>> values;

            public int getPosition() {
                return position;
            }

            public void setPosition(int position) {
                this.position = position;
            }

            public String getLabel() {
                return label;
            }

            public void setLabel(String label) {
                this.label = label;
            }

            public List<List<String>> getValues() {
                return values;
            }

            public void setValues(List<List<String>> values) {
                this.values = values;
            }
        }
        public static class AgeBean {


            private int position;
            private String label;
            private List<List<String>> values;

            public int getPosition() {
                return position;
            }

            public void setPosition(int position) {
                this.position = position;
            }

            public String getLabel() {
                return label;
            }

            public void setLabel(String label) {
                this.label = label;
            }

            public List<List<String>> getValues() {
                return values;
            }

            public void setValues(List<List<String>> values) {
                this.values = values;
            }
        }

        public static class FeatureBean {


            private int position;
            private String label;
            private List<List<String>> values;

            public int getPosition() {
                return position;
            }

            public void setPosition(int position) {
                this.position = position;
            }

            public String getLabel() {
                return label;
            }

            public void setLabel(String label) {
                this.label = label;
            }

            public List<List<String>> getValues() {
                return values;
            }

            public void setValues(List<List<String>> values) {
                this.values = values;
            }
        }
    }
}
