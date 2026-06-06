package uz.thompson.appmockielts.service.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.thompson.appmockielts.entity.*;
import uz.thompson.appmockielts.enums.ProblemType;
import uz.thompson.appmockielts.exception.RestException;
import uz.thompson.appmockielts.repository.TrainingOptionRepository;
import uz.thompson.appmockielts.repository.TrainingProblemRepository;
import uz.thompson.appmockielts.repository.TrainingRepository;
import uz.thompson.appmockielts.service.TrainingProblemService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingProblemServiceImpl implements TrainingProblemService {

    private final TrainingProblemRepository problemRepository;
    private final TrainingOptionRepository optionRepository;
    private final TrainingRepository trainingRepository;

    @Override
    @Transactional
    public String addProblems(String sectionData, Training training, Integer problemCount) {
        log.info("TrainingProblemServiceImpl.addProblems: sectionData = {}, sectionParent = {}, problemCount = {}", sectionData, training.getId(), problemCount);

        if (sectionData == null || sectionData.trim().isEmpty()) {
            log.error("Section data is empty for section: {}", training.getId());
            throw RestException.restThrow("SECTION_DATA_EMPTY");
        }

        Document document = Jsoup.parseBodyFragment(sectionData);

        // Parse problems and update HTML in one pass
        parseAndUpdateProblems(document, training);

        return document.body().outerHtml();
    }

    @Override
    @Transactional
    public String updateProblems(String sectionData, Training training, Integer problemCount) {
        log.info("TrainingProblemServiceImpl.updateProblems: sectionData = {}, sectionParent = {}, problemCount = {}", sectionData, training.getId(), problemCount);

        if (sectionData == null || sectionData.trim().isEmpty())
            return training.getSectionData();

        if (Objects.equals(sectionData.trim(), training.getOriginalSectionData()))
            return training.getSectionData();

        optionRepository.deleteAllBySectionId(training.getId());
        problemRepository.deleteBySectionId(training.getId());

        return addProblems(sectionData, training, problemCount);
    }

    private void parseAndUpdateProblems(Document document, Training training) {
        log.info("TrainingProblemServiceImpl.parseAndUpdateProblems: document = {}, training = {}", document, training.getId());

        // Find all problem elements by their tags
        Elements problemElements = document.select("input[type=text], select-blot, checkbox-blot, radio-blot");

//        if (problemElements.size() != problemCount)
//            throw RestException.restThrow("Problem count has to be equal to " + problemCount + " for this section");

        long problemCount = problemElements.stream()
                .peek(element -> parseAndUpdateProblem(element, training))
                .map(element -> {
                    if (Objects.equals(element.tagName(), "checkbox-blot")) {
                        return 2;
                    }
                    return 1;
                })
                .reduce(0, Integer::sum);

        training.setProblemCount(problemCount);
        trainingRepository.save(training);
    }

    private void parseAndUpdateProblem(Element problemElement, Training training) {
        log.info("TrainingProblemServiceImpl.parseAndUpdateProblems: problemElement = {}, sectionParent = {}", problemElement, training.getId());

        try {
            ProblemType problemType = detectProblemType(problemElement);

            TrainingProblem problem = TrainingProblem.builder()
                    .problemType(problemType)
                    .training(training)
                    .orderIndex(problemElement.attr("name"))
                    .build();

            problem = problemRepository.save(problem);

            // Parse options based on problem type
            parseOptions(problem, problemElement);

            // Add problem ID to the problem element
            problemElement.attr("data-problem-id", problem.getId().toString());

            problem.setCorrectAnswers(optionRepository.countByProblem_IdAndCorrectTrue(problem.getId()));
            problemRepository.save(problem);

            // Remove correctness markers
            removeCorrectnessMarkers(problemElement);

        } catch (Exception e) {
            log.error("Problem parsing problem element: {}", problemElement.outerHtml(), e);
            throw RestException.restThrow(e.getMessage());
        }
    }

    private ProblemType detectProblemType(Element problemElement) {
        log.info("TrainingProblemServiceImpl.detectProblemType: problemElement = {}", problemElement);

        String tagName = problemElement.tagName();

        return switch (tagName) {
            case "select-blot" -> ProblemType.SELECT;
            case "radio-blot" -> ProblemType.RADIO;
            case "checkbox-blot" -> ProblemType.MULTIPLE_CHOICE;
            default -> ProblemType.INPUT;
        };
    }

    private void parseOptions(TrainingProblem problem, Element problemElement) {
        log.info("TrainingProblemServiceImpl.parseOptions: problem = {}, problemElement = {}", problem.getId(), problemElement);

        switch (problem.getProblemType()) {
            case INPUT -> parseInputOptions(problem, problemElement);
            case SELECT -> parseSelectOptions(problem, problemElement);
            case RADIO -> parseRadioOptions(problem, problemElement);
            case MULTIPLE_CHOICE -> parseCheckboxOptions(problem, problemElement);
        }
    }

    private void parseInputOptions(TrainingProblem problem, Element inputElement) {
        log.info("TrainingProblemServiceImpl.parseInputOptions: problem = {}, inputElement = {}", problem.getId(), inputElement);

        // For input type, the correct answer is in the value attribute with '/' delimiter
        String value = inputElement.attr("value").trim();
        String name = inputElement.attr("name").trim();
        List<TrainingOption> options = Arrays.stream(value.split("/"))
                .map(s -> TrainingOption.builder()
                        .problem(problem)
                        .option(s.trim())
                        .orderIndex(name)
                        .correct(true)
                        .build())
                .toList();

        optionRepository.saveAll(options);

        inputElement.removeAttr("value");
    }

    private void parseSelectOptions(TrainingProblem problem, Element selectBlot) {
        log.info("TrainingProblemServiceImpl.parseSelectOptions: problem = {}, selectBlot = {}", problem.getId(), selectBlot);

        String correctOption = selectBlot.attr("data-correct-option").trim();
        // Find all option elements within the select
        Elements optionElements = selectBlot.select("option");

        optionElements.forEach(optionElement -> {
            String optionValue = optionElement.attr("value").trim();
            String name = optionElement.attr("name").trim();

            TrainingOption option = TrainingOption.builder()
                    .option(optionValue)
                    .orderIndex(name)
                    .problem(problem)
                    .correct(Objects.equals(correctOption, optionValue))
                    .build();

            optionRepository.save(option);
            // Add option ID to option element
            optionElement.attr("data-option-id", option.getId().toString());
        });
    }

    private void parseRadioOptions(TrainingProblem problem, Element radioBlot) {
        log.info("ProblemServiceImpl.parseRadioOptions: problem = {}, radioBlot = {}", problem.getId(), radioBlot);

        String correctOption = radioBlot.attr("data-correct-option").trim();

        // Find all radio items
        Elements radioItems = radioBlot.select("input[type=radio]");

        radioItems.forEach(item -> {
            String optionValue = item.attr("value").trim();
            String name = item.attr("name").trim();

            TrainingOption option = TrainingOption.builder()
                    .problem(problem)
                    .orderIndex(name)
                    .option(optionValue)
                    .correct(Objects.equals(correctOption, optionValue))
                    .build();

            optionRepository.save(option);

            // Add option ID to radio input
            item.attr("data-option-id", option.getId().toString());
        });
    }

    private void parseCheckboxOptions(TrainingProblem problem, Element checkboxBlot) {
        log.info("ProblemServiceImpl.parseCheckboxOptions: problem = {}, checkboxBlot = {}", problem.getId(), checkboxBlot);

        String correctOptionsJson = checkboxBlot.attr("data-correct-options");
        List<String> correctOptions = parseCorrectOptions(correctOptionsJson);
        if (correctOptions.size() != 2) {
            throw RestException.restThrow("Problem with type multiple choice has more or less than 2 correct options");
        }
        // Find all checkbox items
        Elements checkboxItems = checkboxBlot.select("input[type=checkbox]");

        checkboxItems.forEach(item -> {
            String optionValue = item.attr("value").trim();
            String name = item.attr("name").trim();

            TrainingOption option = TrainingOption.builder()
                    .problem(problem)
                    .orderIndex(name)
                    .option(optionValue)
                    .correct(correctOptions.contains(optionValue))
                    .build();

            optionRepository.save(option);
            // Add option ID to checkbox input
            item.attr("data-option-id", option.getId().toString());
        });

    }

    private List<String> parseCorrectOptions(String correctOptionsJson) {
        log.info("TrainingProblemServiceImpl.parseCorrectOptions: correctOptionsJson = {}", correctOptionsJson);

        List<String> correctOptions = new ArrayList<>();

        if (correctOptionsJson == null || correctOptionsJson.isEmpty()) {
            return correctOptions;
        }

        try {
            // Remove the JSON array brackets and quotes
            String cleaned = correctOptionsJson
                    .replace("[", "")
                    .replace("]", "")
                    .replace("\"", "")
                    .replace("&quot;", "");

            // Split by comma
            String[] options = cleaned.split(",");
            for (String option : options) {
                correctOptions.add(option.trim());
            }
        } catch (Exception e) {
            System.err.println("Error parsing correct options: " + e.getMessage());
        }
        return correctOptions;
    }


    private void removeCorrectnessMarkers(Element problemElement) {
        log.info("TrainingProblemServiceImpl.removeCorrectnessMarkers: problemElement = {}", problemElement);

        // Remove data-correct-* attributes
        problemElement.removeAttr("data-correct-option");
        problemElement.removeAttr("data-correct-options");

        // Remove checked/selected attributes from child elements
        problemElement.select("[checked]").removeAttr("checked");
        problemElement.select("[selected]").removeAttr("selected");
    }
}
