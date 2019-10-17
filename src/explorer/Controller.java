package explorer;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;


public class Controller implements Initializable {

    //FXML variables
    @FXML
    private TreeView<Path> filesTree;
    @FXML
    private Label bottomLabel;

    //constants
    private static final String rootFolder = "C:/";

    //current positions in tree
    private TreeItem<Path> currentNode;
    private String currentFolder;

    //icons
    private final Node rootIcon = new ImageView(new Image(getClass().getResourceAsStream("/res/root.png"), 16, 16, false, false));
    private final Image folderClosedIcon = new Image(getClass().getResourceAsStream("/res/folderClosed.png"), 16, 16, false, false);
    private final Image waitIcon = new Image(getClass().getResourceAsStream("/res/wait.png"), 16, 16, false, false);
    private final Image folderOpenedIcon = new Image(getClass().getResourceAsStream("/res/folderOpened.png"), 16, 16, false, false);
    private final Image fileIcon = new Image(getClass().getResourceAsStream("/res/file.png"), 16, 16, false, false);

    @Override
    public void initialize(URL location, ResourceBundle resources){
        TreeItem<Path> treeItem = new TreeItem<>(Paths.get(rootFolder), rootIcon);
        treeItem.setExpanded(false);
        createTree(treeItem);

        //alphabetical sort of tree elements
        treeItem.getChildren().sort(Comparator.comparing(new Function<TreeItem<Path>, String>() {
            @Override
            public String apply(TreeItem<Path> t) {
                return t.getValue().toString().toLowerCase();
            }
        }));
        filesTree.setRoot(treeItem);

        //getting selected file info
        filesTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Path>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<Path>> observable, TreeItem<Path> oldValue, TreeItem<Path> newValue) {
                bottomLabel.setText(newValue.getValue().toString());
                if ((Files.isDirectory(newValue.getValue()))&&(newValue.isLeaf())) {
                    newValue.setGraphic(new ImageView(waitIcon));

                    Timeline timeline = new Timeline(new KeyFrame(
                            Duration.millis(2000),
                            ae -> createTree(newValue)));
                    timeline.play();
                }
                currentFolder = newValue.getValue().toString();
                currentNode = newValue;
                currentNode = newValue;
            }
        });
    }

    public void createTree(TreeItem<Path> rootItem) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(rootItem.getValue())) {
            for (Path path : directoryStream) {
                if(Files.isDirectory(path))
                    addNode(path, rootItem, folderClosedIcon);
                else
                    addNode(path, rootItem, fileIcon);
            }
        }
        catch(IOException ex) {
            bottomLabel.setText(ex.getMessage());
            ex.printStackTrace();
        }
        if(!rootItem.getValue().equals(Paths.get(rootFolder)))
            rootItem.setGraphic(new ImageView(folderOpenedIcon));
    }

    public void handleCreateFolder(){
        if(currentFolder!=""){
            TextInputDialog dialog = new TextInputDialog("new folder");
            dialog.setTitle("Folder Name Input Dialog");
            dialog.setHeaderText("Entering a name for new folder");
            dialog.setContentText("Please enter new folder name:");
            Optional<String> result = dialog.showAndWait();
            String newFolderName = "";
            if (result.isPresent()){
                newFolderName = result.get().toString();
                newFolderName = result.get().toString();
            }

            if((newFolderName!="")&&(!newFolderName.contains("/"))&&(!newFolderName.contains("\\"))
                    &&(!newFolderName.contains(":"))&&(!newFolderName.contains("?"))
                    &&(!newFolderName.contains("*"))&&(!newFolderName.contains("\""))
                    &&(!newFolderName.contains("|"))&&(!newFolderName.contains("+"))
                    &&(!newFolderName.contains(">"))&&(!newFolderName.contains("<"))) {
                Path path = Paths.get(currentFolder + "/" + newFolderName);
                if (!Files.exists(path)) {
                    try {
                        Files.createDirectories(path);

                        addNode(path, currentNode, folderClosedIcon);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                throwAlert(Alert.AlertType.ERROR,"New Folder Error", "Invalid folder name!",
                        "Press File-New Folder again and type valid name.");
            }
        }
        else{
            throwAlert(Alert.AlertType.ERROR,"New Folder Error", "No parent folder selected!",
                    "Select a parent folder first, by clicking on it, then press File-New Folder again.");
        }
    }

    public void handleAbout(){
        throwAlert(Alert.AlertType.INFORMATION, "Information", null, "This program is created by Yan" +
                "Stetsyuk as a test for SPB-Splat company");
    }


    public void addNode(Path path, TreeItem<Path> parent, Image icon){
        TreeItem<Path> newItem = new TreeItem<>(path, new ImageView(icon));
        if(icon.equals(folderClosedIcon)) {
            newItem.setExpanded(true);
            newItem.expandedProperty().addListener(new ChangeListener<Boolean>() {

                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        newItem.setGraphic(new ImageView(folderOpenedIcon));
                    }
                    else {
                        newItem.setGraphic(new ImageView(folderClosedIcon));
                    }
                }

            });
        }
        parent.getChildren().add(newItem);
    }

    public void throwAlert(Alert.AlertType alertType, String title, String header, String text) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(text);
        alert.showAndWait();
    }
}