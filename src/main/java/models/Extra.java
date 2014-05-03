package models;

/**
 * 
 * @author svenkubiak
 *
 */
import java.util.Date;
import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "extras", noClassnameStored = true)
public class Extra {
    @Id
    private ObjectId id;

    @Reference("extra_extratips")
    private List<ExtraTip> extraTips;

    @NotNull
    private String question;

    @NotNull
    private String questionShort;

    @Min(value = 0)
    private int points;

    @NotNull
    private Date ending;

    @Reference("extra_answers")
    private List<Team> answers;

    @NotNull
    private String extraReference;

    @Reference("extra_gamereferences")
    private List<Game> gameReferences;

    @Reference
    private Team answer;

    public List<ExtraTip> getExtraTipps() {
        return extraTips;
    }

    public void setExtraTipps(List<ExtraTip> extraTips) {
        this.extraTips = extraTips;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getQuestionShort() {
        return questionShort;
    }

    public void setQuestionShort(String questionShort) {
        this.questionShort = questionShort;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Date getEnding() {
        return ending;
    }

    public void setEnding(Date ending) {
        this.ending = ending;
    }

    public List<Team> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Team> answers) {
        this.answers = answers;
    }

    public String getExtraReference() {
        return extraReference;
    }

    public void setExtraReference(String extraReference) {
        this.extraReference = extraReference;
    }

    public List<Game> getGameReferences() {
        return gameReferences;
    }

    public void setGameReferences(List<Game> gameReferences) {
        this.gameReferences = gameReferences;
    }

    public Team getAnswer() {
        return answer;
    }

    public void setAnswer(Team answer) {
        this.answer = answer;
    }

    public boolean isTipable() {
        if (new Date().getTime() >= this.ending.getTime()) {
            return false;
        }

        return true;
    }

    //TODO Refactoring
    //    public String questionUnescaped() {
    //        if (StringUtils.isNotBlank(this.question)) {
    //            String question = Messages.get(this.question);
    //            return StringEscapeUtils.unescapeHtml(question);
    //        }
    //
    //        return "";
    //    }
}