package zerobase.weather.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import zerobase.weather.domain.Diary;
import zerobase.weather.service.DiaryService;

import java.time.LocalDate;
import java.util.List;

@RestController
public class DiaryController {
        private final DiaryService diaryService;

        public DiaryController(DiaryService diaryService) {
            this.diaryService = diaryService;
        }

        @ApiOperation("날씨 일기를 기록")
        @PostMapping("/create/diary")
        void createDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "기록할 날짜의 지정", example = "2023-10-30") LocalDate date, @RequestBody @ApiParam(value = "일기 기록", example = "Goodday!") String text){
            diaryService.createDiary(date, text);
        }

        @ApiOperation("지정 날짜의 날씨 일기를 읽기")
        @GetMapping("/read/diary")
        List<Diary> readDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "조회할 날짜의 지정", example = "2023-10-30") LocalDate date){
            return diaryService.readDiary(date);
        }

        @ApiOperation("지정 범위의 날짜의 날씨 일기를 읽기")
        @GetMapping("/read/diaries")
        List<Diary> readDiaries(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "조회할 날짜의 시작", example = "2023-10-30") LocalDate startDate,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "조회할 날짜의 끝", example = "2023-10-31") LocalDate endDate){
            return diaryService.readDiaries(startDate, endDate);
        }

        @ApiOperation("지정 날짜의 날씨 일기를 수정")
        @PutMapping("/update/diary")
        void updateDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "수정할 날짜의 지정", example = "2023-10-30") LocalDate date, @RequestBody @ApiParam(value = "수정할 일기 내용을 기록", example = "Awesomeday!") String text){
            diaryService.updateDiary(date, text);
        }

        @ApiOperation("지정 날짜의 날씨 일기를 삭제")
        @DeleteMapping("/delete/diary")
        void deleteDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "삭제할 날짜의 지정", example = "2023-10-30") LocalDate date){
            diaryService.deleteDiary(date);
        }
}
