package view.audio;

import com.jfoenix.controls.JFXButton;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.db.DBUtils;
import model.engine.AudioUtils;
import model.engine.datastructures.Song;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/** A BorderPane class which is used to display all songs
 *  both from the music folder and those who are only in the DB
 *  in the GUI. It supports paging.
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class SongCatalogue extends BorderPane {
    // the number of songs to be displayed per page
    private static int MAX_SONGS_PER_PAGE;
    static {
        MAX_SONGS_PER_PAGE = 5;
    }

    // all of the pages
    private ArrayList<VBox> pages;
    // current page index
    private int currentIndex;
    // the label that shows which page it is
    private Label pageLabel;
    // prev and next buttons
    private JFXButton prev, next;

    /**
     * Constructor
     */
    public SongCatalogue() {
        // dimensions of the catalogue
        setPrefWidth(500);
        setMaxWidth(500);

        // get a list of all audio from DB and from Dir
        LinkedList<Song> listSongs = new LinkedList<>();

        // get all songs from song dir
        String[] songsInDir = AudioUtils.scanForSongs();
        assert songsInDir != null;
        for(String song : songsInDir) {
            listSongs.addLast(new Song(song, false, true));
        }

        // get all songs from DB
        String[] songsInDB = DBUtils.getSongsInDB();
        if(songsInDB != null) {
            for (String song : songsInDB) {
                boolean added = false;

                // if the song is already in the list, only change boolean value
                for (Song songInList : listSongs) {
                    if (songInList.getName().equals(song)) {
                        songInList.setInDB();
                        added = true;
                    }
                }

                // if its not already in the list, add it
                if (!added) {
                    listSongs.addLast(new Song(song, true, false));
                }
            }

        }

        // convert to array
        Song[] songs = listSongs.toArray(new Song[0]);

        // add prev and next buttons if necessary
        if(songs.length > MAX_SONGS_PER_PAGE) {
            addArrowButtons();
        }

        // init the pages array list
        pages = new ArrayList<>();
        currentIndex = 0;

        // loop & create all the pages required
        for(int i = 0; i < songs.length; i += MAX_SONGS_PER_PAGE) {
            VBox page = new VBox();
            page.setSpacing(10);
            page.setMinHeight(260);
            page.setAlignment(Pos.CENTER);
            page.getStyleClass().add("song-grid");

            int lastSongOfPage = MAX_SONGS_PER_PAGE + i;
            if(lastSongOfPage > songs.length) lastSongOfPage = songs.length;
            Song[] songsInPage = Arrays.copyOfRange(songs, i, lastSongOfPage);

            // add all of the songs to the page
            for(Song song : songsInPage) {
                page.getChildren().add(new SongWrap(song));
            }

            // add the page to the list
            pages.add(page);
        }

        // set the current page (0) to be the center node
        setCenter(pages.get(currentIndex));

        // add a page label, to display which page we are at, as a bottom node
        pageLabel = new Label();
        pageLabel.getStyleClass().add("page-lbl");
        setBottom(pageLabel);
        updatePageLabel();
        setAlignment(getBottom(), Pos.CENTER);

        // style class for the border pane
        getStyleClass().add("song-catalogue");
    }

    /**
     * A method to display which is the current page
     */
    private void updatePageLabel() {
        pageLabel.setText("Page " + (currentIndex + 1) + "/" + pages.size());
    }

    /**
     * A method that adds arrow buttons used to go through pages
     */
    private void addArrowButtons() {
        // the previous button
        prev = new JFXButton();
        Image leftArrow = new Image(getClass().getResource("./../img/left.png").toExternalForm());
        prev.setGraphic(new ImageView(leftArrow));
        prev.setOnAction(this::previous);
        prev.setDisable(true);
        prev.getStyleClass().add("arrow-btn");

        // wrap it in a stack pane (for alignment purposes) and add it as a left node
        StackPane left = new StackPane(prev);
        left.setPrefWidth(100);
        left.setAlignment(Pos.CENTER);
        setLeft(left);
        setAlignment(getLeft(), Pos.CENTER);

        // the next button
        next = new JFXButton();
        Image rightArrow = new Image(getClass().getResource("./../img/right.png").toExternalForm());
        next.setGraphic(new ImageView(rightArrow));
        next.setOnAction(this::next);
        next.getStyleClass().add("arrow-btn");

        // wrap it in a stack pane (for alignment purposes) and add it as a left node
        StackPane right = new StackPane(next);
        right.setAlignment(Pos.CENTER);
        right.setPrefWidth(100);
        setRight(right);
        setAlignment(getRight(), Pos.CENTER);
    }

    /**
     * A method which goes to the previous page
     * @param e the previous button
     */
    @SuppressWarnings("unused")
    private void previous(ActionEvent e) {
        next.setDisable(false);
        currentIndex --;
        setCenter(pages.get(currentIndex)); // display the next page
        transitionCenter(); // transition
        if(currentIndex == 0) prev.setDisable(true);
        updatePageLabel();
    }

    /**
     * A method which goes to the next page
     * @param e the next button
     */
    @SuppressWarnings("unused")
    private void next(ActionEvent e) {
        prev.setDisable(false);
        currentIndex++;
        setCenter(pages.get(currentIndex)); // display the next page
        transitionCenter(); // transition
        if(currentIndex == pages.size() - 1) next.setDisable(true);
        updatePageLabel();
    }

    /**
     * A simple transition which scales the center node
     * from 0 to 1 by the X for 200ms. Used when changing pages.
     */
    private void transitionCenter() {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), getCenter());
        st.setFromX(0f);
        st.setByX(1f);
        st.play();
    }
}
