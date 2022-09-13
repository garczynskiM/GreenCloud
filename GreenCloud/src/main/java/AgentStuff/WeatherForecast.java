package AgentStuff;

import java.util.*;

public class WeatherForecast {
    public Map<String, Double> weather_status = new HashMap<String, Double>() {{
        put("SUNNY", 1.0);
        put("CLOUDY", 0.5);
        put("RAINY", 0.3);
        put("FOGGY", 0.2);
        put("NIGHT", 0.0);
    }};

    public String current_weather;
    public double current_weather_factor;
    public LinkedList<String> forecast_list = new LinkedList<String>(); //weather forecast list

    private Random random = new Random();

    //time duration is parameter of forecast time, ie. 5 -> forecast for next 5 hours
    public WeatherForecast(int forecast_duration, String start_weather, int seed) {
        random = new Random(seed);
        forecast_init(forecast_duration, start_weather);
    }
    public WeatherForecast(int forecast_duration, String start_weather) {
        forecast_init(forecast_duration, start_weather);
    }
    public WeatherForecast(int forecast_duration, int seed) {
        this(forecast_duration, "SUNNY", seed);
    }
    public WeatherForecast(int forecast_duration) {
        this(forecast_duration, "SUNNY");
    }

    private void forecast_init(int forecast_duration, String start_weather) {
        create_forecast(forecast_duration, weather_status.get(start_weather));
        current_weather = forecast_duration > 0 ? forecast_list.get(0) : "ERROR";
        current_weather_factor = weather_status.get(current_weather);
    }

    private void create_forecast(int forecast_duration, double start_weather){
        create_forecast(forecast_duration, start_weather, 100);
    }
    private void create_forecast(int forecast_duration, double start_weather, int seed) {
        double last_random = start_weather;
        for(int i=0; i<forecast_duration; i++) {
            double next_random = random.nextGaussian()*0.66+last_random;

            if(next_random <0.0) {
                forecast_list.add("NIGHT");
            }
            else if(next_random < 0.2) {
                forecast_list.add("FOGGY");
            }
            else if(next_random < 0.3) {
                forecast_list.add("RAINY");
            }
            else if(next_random < 0.5) {
                forecast_list.add("CLOUDY");
            }
            else {
                forecast_list.add("SUNNY");
            }
            //last_random = Math.min(next_random, 1.0);
            last_random = Math.max(-0.5, Math.min(next_random, 1.0));
        }
    }
    //used to expand forecast range, for example we need forecast for 10hours when we only have for 5h
    public void expand_forecast(int additional_forecast_duration) {
        create_forecast(additional_forecast_duration, weather_status.get(forecast_list.getLast()));
    }
    //update forecast every agent tick
    public void hour_passed_weather_update() {
        //remove first hour
        double last_random = weather_status.get(forecast_list.get(0));
        forecast_list.getFirst();
        double change_chance = 0.02;
        double change_change_factor = 0.01;
        for(int i=0; i< forecast_list.size(); i++) {
            //check if we change weather in given hour
            if(random.nextDouble() <= change_chance) {
                double next_random = random.nextGaussian()*1.5+last_random;
                if(next_random <0.0) {
                    forecast_list.set(i, "NIGHT");
                }
                else if(next_random < 0.2) {
                    forecast_list.set(i, "FOGGY");
                }
                else if(next_random < 0.3) {
                    forecast_list.set(i, "RAINY");
                }
                else if(next_random < 0.5) {
                    forecast_list.set(i, "CLOUDY");
                }
                else {
                    forecast_list.set(i, "SUNNY");
                }
            }
            change_change_factor *= 1.5;
            change_chance += change_change_factor;
            last_random = weather_status.get(forecast_list.get(i));
        }
        current_weather_factor = weather_status.get(forecast_list.get(0));
    }
}
