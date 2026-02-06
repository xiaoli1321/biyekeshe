package com.learnplatform.controller;

import com.learnplatform.entity.Chapter;
import com.learnplatform.service.ChapterService;
import com.learnplatform.service.ProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/chapters")
public class ChapterController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private ProgressService progressService;

    @GetMapping("/{id}")
    public String viewChapter(@PathVariable String id, Model model) {
        Chapter chapter = chapterService.findChapterById(id)
                .orElseThrow(() -> new IllegalArgumentException("章节未找到"));

        model.addAttribute("chapter", chapter);
        return "chapters/view";
    }

    @PostMapping("/{id}/start")
    public String startChapter(@PathVariable String id,
                                   @RequestParam String userId,
                                   @RequestParam String courseId,
                                   Model model) {
        Chapter chapter = chapterService.findChapterById(id)
                .orElseThrow(() -> new IllegalArgumentException("章节未找到"));

        progressService.startChapter(userId, courseId, id);

        model.addAttribute("chapter", chapter);
        return "redirect:/chapters/" + id;
    }

    @PostMapping("/{id}/complete")
    public String completeChapter(@PathVariable String id,
                                      @RequestParam String userId,
                                      @RequestParam String courseId,
                                      @RequestParam(defaultValue = "100") Double score,
                                      @RequestParam(defaultValue = "30") Integer minutes) {
        progressService.completeChapter(userId, courseId, id, score, minutes);
        return "redirect:/chapters/" + id;
    }
}