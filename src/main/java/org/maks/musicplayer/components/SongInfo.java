package org.maks.musicplayer.components;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.maks.musicplayer.model.SongPlayer;

public class SongInfo extends HBox {

    private final RoundedImageView songAvatar = new RoundedImageView();
    private final Label songName = new Label();
    private final Label songAuthor = new Label();

    public SongInfo() {
        load();
    }

    public void bind(ObjectProperty<SongPlayer> mediaPlayerContainerProperty) {
        mediaPlayerContainerProperty.addListener((
                _,
                _,
                mediaPlayerContainer) ->
                load(mediaPlayerContainer)
        );
    }

    public void load(SongPlayer songPlayer) {
        if (songPlayer == null) {
            return;
        }

        songAvatar.setImage(songPlayer.songAvatar());
        songName.setText(songPlayer.songName());
        songAuthor.setText(songPlayer.songAuthor());
    }

    private void load() {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);
        getStyleClass().add("music-list-component");

        songAvatar.setFitWidth(75);
        songAvatar.setFitHeight(75);
        songAvatar.setPreserveRatio(true);

        songName.setTextFill(Color.rgb(230, 230, 230));
        songName.setFont(new Font("Verdana", 17));

        songAuthor.setTextFill(Color.rgb(200, 200, 200));
        songAuthor.setFont(new Font("Verdana", 13));

        VBox labels = new VBox(songName, songAuthor);
        labels.setAlignment(Pos.CENTER_LEFT);
        labels.setSpacing(4);

        getChildren().addAll(songAvatar, labels);
    }

}
