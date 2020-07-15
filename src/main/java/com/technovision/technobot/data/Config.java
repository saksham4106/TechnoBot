package com.technovision.technobot.data;

public class Config {

    public static final DataSave BOT_CONFIG = new Configuration("data/config/","botconfig.json")  {
        @Override
        public void load() {
            super.load();

            if(getJson().getString("token")==null) getJson().put("token", "");
            if(getJson().getString("logs-webhook")==null) getJson().put("logs-webhook", "");
        }
    };
}
