package expose.model.meta;

public interface Command {

    Diagram expo();

    void addModel(UniversalModel model);

    void removeModel(String id);
}
