package zerobase.weather.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.WeatherApplication;
import zerobase.weather.domain.DateWeather;
import zerobase.weather.domain.Diary;
import zerobase.weather.repository.DateWeatherRepository;
import zerobase.weather.repository.DiaryRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiaryService {
    //api key 및 객체선언
    @Value("${openweathermap.key}")
    private String apikey;
    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;
    private static final Logger logger = LoggerFactory.getLogger(WeatherApplication.class);
    //생성자
    public DiaryService(DiaryRepository diaryRepository, DateWeatherRepository dateWeatherRepository) {
        this.diaryRepository = diaryRepository;
        this.dateWeatherRepository = dateWeatherRepository;
    }
    //매일 1시 날씨저장
    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void saveWeatherDate(){
        dateWeatherRepository.save(getWeatherFromApi());
        logger.info("Getweatherfromapi is successed");
    }
    //Diary 저장
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createDiary(LocalDate date, String text){
        logger.info("started to create diary");
        // 날씨 데이터 DB에서 가져오기
        DateWeather dateWeather = getDateWeather(date);

        //파싱된 데이터 + 일기 값 우리 db에 넣기
        Diary nowDiary = new Diary();
        nowDiary.setDateWeather(dateWeather);
        nowDiary.setText(text);
        diaryRepository.save(nowDiary);
        logger.info("end to create diary");
    }
    // 날씨 데이터 가져오기
    @Transactional(isolation = Isolation.SERIALIZABLE)
    private DateWeather getWeatherFromApi(){
        //open weather map에서 날씨 데이터 가져오기
        String weatherData = getWeatherString();

        //받아온 날씨 json 파싱하기
        Map<String, Object> parsedWeather = parseWeather(weatherData);
        DateWeather dateWeather = new DateWeather();
        dateWeather.setDate(LocalDate.now());
        dateWeather.setWeather(parsedWeather.get("main").toString());
        dateWeather.setIcon(parsedWeather.get("icon").toString());
        dateWeather.setTemperature((double) parsedWeather.get("temp"));
        return dateWeather;
    }
    //날씨 데이터 매일 1시 저장을 위한
    private DateWeather getDateWeather(LocalDate date){
        List<DateWeather> dateWeatherListFromDB = dateWeatherRepository.findAllByDate(date);
        if(dateWeatherListFromDB.size() == 0){
            return getWeatherFromApi();
        } else {
            return dateWeatherListFromDB.get(0);
        }
    }
    //다이어리 읽기
    @Transactional(readOnly = true)
    public List<Diary> readDiary(LocalDate date){
        logger.info("read diary "+date);
        return diaryRepository.findAllByDate(date);
    }
    //다이어리 읽기(날짜지정)
    @Transactional(readOnly = true)
    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate){
        logger.info("read diaries "+startDate+" to "+endDate);
        return diaryRepository.findAllByDateBetween(startDate, endDate);

    }
    //다이어리 수정
    public void updateDiary(LocalDate date, String text){
        Diary nowDiary = diaryRepository.getFirstByDate(date);
        nowDiary.setText(text);
        diaryRepository.save(nowDiary);
        logger.info("Update complete");
    }
    //다이어리 삭제
    public void deleteDiary(LocalDate date){
        diaryRepository.deleteAllByDate(date);
        logger.info(date+" has been deleted");
    }
    //openweatherapi에서 정보 가져오기
    private String getWeatherString() {
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid=" + apikey;

        try {

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            BufferedReader br;

            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            return response.toString();

        } catch (Exception e) {
            return "failed to get response";
        }
    }
    //파싱
    private Map<String, Object> parseWeather(String jsonString) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch(ParseException e){
            throw new RuntimeException(e);
        }

        Map<String, Object> resultMap = new HashMap<>();

        JSONObject mainData = (JSONObject) jsonObject.get("main");
        resultMap.put("temp", mainData.get("temp"));
        JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
        JSONObject weatherData = (JSONObject) weatherArray.get(0);
        resultMap.put("main", weatherData.get("main"));
        resultMap.put("icon", weatherData.get("icon"));
        return resultMap;
    }
}
