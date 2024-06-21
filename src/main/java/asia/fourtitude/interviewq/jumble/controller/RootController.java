package asia.fourtitude.interviewq.jumble.controller;

import java.time.ZonedDateTime;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import asia.fourtitude.interviewq.jumble.constant.ApplicationConstant;
import asia.fourtitude.interviewq.jumble.core.JumbleEngine;
import asia.fourtitude.interviewq.jumble.model.ExistsForm;
import asia.fourtitude.interviewq.jumble.model.PrefixForm;
import asia.fourtitude.interviewq.jumble.model.ScrambleForm;
import asia.fourtitude.interviewq.jumble.model.SearchForm;
import asia.fourtitude.interviewq.jumble.model.SubWordsForm;

@Controller
@RequestMapping(path = "/")
public class RootController {

    private static final Logger LOG = LoggerFactory.getLogger(RootController.class);

    private final JumbleEngine jumbleEngine;

    @Autowired(required = true)
    public RootController(JumbleEngine jumbleEngine) {
        this.jumbleEngine = jumbleEngine;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("timeNow", ZonedDateTime.now());
        return "index";
    }

    @GetMapping(ApplicationConstant.JUMBLE_SCRAMBLE)
    public String doGetScramble(Model model) {
        model.addAttribute("form", new ScrambleForm());
        return ApplicationConstant.JUMBLE_SCRAMBLE;
    }

    @PostMapping(ApplicationConstant.JUMBLE_SCRAMBLE)
    public String doPostScramble(@Valid
            @ModelAttribute(name = "form") ScrambleForm form,
            BindingResult bindingResult, Model model) {

        if(bindingResult.hasErrors())
            return ApplicationConstant.JUMBLE_SCRAMBLE;

        form.setScramble(jumbleEngine.scramble(form.getWord()));    
        return ApplicationConstant.JUMBLE_SCRAMBLE;
    }

    @GetMapping(ApplicationConstant.JUMBLE_PALINDROME)
    public String doGetPalindrome(Model model) {
        model.addAttribute("words", this.jumbleEngine.retrievePalindromeWords());
        return ApplicationConstant.JUMBLE_PALINDROME;
    }

    @GetMapping(ApplicationConstant.JUMBLE_EXISTS)
    public String doGetExists(Model model) {
        model.addAttribute("form", new ExistsForm());
        return ApplicationConstant.JUMBLE_EXISTS;
    }

    @PostMapping(ApplicationConstant.JUMBLE_EXISTS)
    public String doPostExists(@Valid
            @ModelAttribute(name = "form") ExistsForm form,
            BindingResult bindingResult, Model model) {

        if(bindingResult.hasErrors())
            return ApplicationConstant.JUMBLE_EXISTS;

        String trimWord = form.getWord().trim();
        if(StringUtils.isBlank(trimWord)) {
            bindingResult.rejectValue("word", "error.word", "Invalid character!");
            return ApplicationConstant.JUMBLE_EXISTS;
        }

        form.setExists(jumbleEngine.exists(trimWord));
        return ApplicationConstant.JUMBLE_EXISTS;
    }

    @GetMapping(ApplicationConstant.JUMBLE_PREFIX)
    public String doGetPrefix(Model model) {
        model.addAttribute("form", new PrefixForm());
        return ApplicationConstant.JUMBLE_PREFIX;
    }

    @PostMapping(ApplicationConstant.JUMBLE_PREFIX)
    public String doPostPrefix(@Valid
            @ModelAttribute(name = "form") PrefixForm form,
            BindingResult bindingResult, Model model) {

        if(bindingResult.hasErrors())
            return ApplicationConstant.JUMBLE_PREFIX;

        String trimPrefix = form.getPrefix().trim();
        if(StringUtils.isBlank(trimPrefix)) {
            bindingResult.rejectValue("prefix", "error.prefix", "Invalid character!");
            return ApplicationConstant.JUMBLE_PREFIX;
        }

        form.setWords(jumbleEngine.wordsMatchingPrefix(trimPrefix));
        return ApplicationConstant.JUMBLE_PREFIX;
    }

    @GetMapping(ApplicationConstant.JUMBLE_SEARCH)
    public String doGetSearch(Model model) {
        model.addAttribute("form", new SearchForm());
        return ApplicationConstant.JUMBLE_SEARCH;
    }

    @PostMapping(ApplicationConstant.JUMBLE_SEARCH)
    public String doPostSearch(@Valid
            @ModelAttribute(name = "form") SearchForm form,
            BindingResult bindingResult, Model model) {
        
        if(bindingResult.hasErrors())
            return ApplicationConstant.JUMBLE_SEARCH;

        Character startChar = form.getStartChar() != null && form.getStartChar().length() == 1 
                ? form.getStartChar().charAt(0) : null;
        Character endChar = form.getEndChar() != null && form.getEndChar().length() == 1 
                ? form.getEndChar().charAt(0) : null;

        if(startChar == null && endChar == null && form.getLength() == null) {
            bindingResult.rejectValue("startChar", "error.startChar", "Invalid startChar");
            bindingResult.rejectValue("endChar", "error.endChar", "Invalid endChar");
            bindingResult.rejectValue("length", "error.length", "Invalid length");
            return ApplicationConstant.JUMBLE_SEARCH;
        }
        
        form.setWords(jumbleEngine.searchWords(startChar, endChar,
                form.getLength()));
        return ApplicationConstant.JUMBLE_SEARCH;
    }

    @GetMapping(ApplicationConstant.JUMBLE_SUBWORDS)
    public String goGetSubWords(Model model) {
        model.addAttribute("form", new SubWordsForm());
        return ApplicationConstant.JUMBLE_SUBWORDS;
    }

    @PostMapping(ApplicationConstant.JUMBLE_SUBWORDS)
    public String doPostSubWords(@Valid
            @ModelAttribute(name = "form") SubWordsForm form,
            BindingResult bindingResult, Model model) {

        if(bindingResult.hasErrors())
            return ApplicationConstant.JUMBLE_SUBWORDS;
        
        String trimWord = form.getWord().trim();
        if(StringUtils.isBlank(trimWord)) {
              bindingResult.rejectValue("word", "error.word", "Invalid character!");
              return ApplicationConstant.JUMBLE_SUBWORDS;
        }

        form.setWords(jumbleEngine.generateSubWords(trimWord, form.getMinLength()));
        return ApplicationConstant.JUMBLE_SUBWORDS;
    }

}
