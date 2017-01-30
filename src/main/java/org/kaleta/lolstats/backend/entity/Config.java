//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.11 at 03:44:07 PM CET 
//


package org.kaleta.lolstats.backend.entity;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "lolApi",
    "seasons",
        "champs"
})
@XmlRootElement(name = "config")
public class Config {
    @XmlElement(name = "lol-api", required = true)
    protected LolApi lolApi= new LolApi();
    @XmlElement(required = true)
    protected Seasons seasons= new Seasons();

    public Champs getChamps() {
        return champs;
    }

    public void setChamps(Champs champs) {
        this.champs = champs;
    }

    @XmlElement(required = true)
    protected Champs champs = new Champs();

    /**
     * Gets the value of the lolApi property.
     * 
     * @return
     *     possible object is
     *     {@link LolApi }
     *     
     */
    public LolApi getLolApi() {
        return lolApi;
    }

    /**
     * Sets the value of the lolApi property.
     * 
     * @param value
     *     allowed object is
     *     {@link LolApi }
     *     
     */
    public void setLolApi(LolApi value) {
        this.lolApi = value;
    }

    /**
     * Gets the value of the seasons property.
     * 
     * @return
     *     possible object is
     *     {@link Seasons }
     *     
     */
    public Seasons getSeasons() {
        return seasons;
    }

    /**
     * Sets the value of the seasons property.
     * 
     * @param value
     *     allowed object is
     *     {@link Seasons }
     *     
     */
    public void setSeasons(Seasons value) {
        this.seasons = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class LolApi {
        @XmlAttribute(name = "region", required = true)
        protected String region="";
        @XmlAttribute(name = "nick", required = true)
        protected String nick="";
        @XmlAttribute(name = "access-key", required = true)
        protected String accessKey="";

        /**
         * Gets the value of the region property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRegion() {
            return region;
        }

        /**
         * Sets the value of the region property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRegion(String value) {
            this.region = value;
        }

        /**
         * Gets the value of the nick property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getNick() {
            return nick;
        }

        /**
         * Sets the value of the nick property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setNick(String value) {
            this.nick = value;
        }

        /**
         * Gets the value of the accessKey property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getAccessKey() {
            return accessKey;
        }

        /**
         * Sets the value of the accessKey property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setAccessKey(String value) {
            this.accessKey = value;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "season"
    })
    public static class Seasons {

        @XmlAttribute(name = "actual-season", required = true)
        protected String actualSeason="";

        protected List<Season> season;

        public List<Season> getSeason() {
            if (season == null) {
                season = new ArrayList<Season>();
            }
            return this.season;
        }

        public String getActualSeason() {
            return actualSeason;
        }

        public void setActualSeason(String actualSeason) {
            this.actualSeason = actualSeason;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Season {
            @XmlAttribute(name = "order", required = true)
            protected String order="";
            @XmlAttribute(name = "id", required = true)
            protected String id="";

            /**
             * Gets the value of the order property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getOrder() {
                return order;
            }

            /**
             * Sets the value of the order property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setOrder(String value) {
                this.order = value;
            }

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getId() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setId(String value) {
                this.id = value;
            }

        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "champ"
    })
    public static class Champs {
        protected List<Champ> champ;

        public List<Champ> getChamp() {
            if (champ == null) {
                champ = new ArrayList<Champ>();
            }
            return this.champ;
        }


        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Champ {
            @XmlAttribute(name = "name", required = true)
            protected String name="";

            /**
             * Gets the value of the name property.
             *
             * @return
             *     possible object is
             *     {@link String }
             *
             */
            public String getName() {
                return name;
            }

            /**
             * Sets the value of the order property.
             *
             * @param value
             *     allowed object is
             *     {@link String }
             *
             */
            public void setName(String value) {
                this.name = value;
            }
        }

    }

}
