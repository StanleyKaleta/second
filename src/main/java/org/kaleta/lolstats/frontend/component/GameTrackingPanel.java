package org.kaleta.lolstats.frontend.component;

import org.kaleta.lolstats.backend.entity.GameInfo;
import org.kaleta.lolstats.backend.entity.Season;
import org.kaleta.lolstats.backend.service.DataSourceService;
import org.kaleta.lolstats.backend.service.LolApiService;
import org.kaleta.lolstats.frontend.dialog.AddGameDialog;

import javax.swing.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 22.02.2016.
 */
public class GameTrackingPanel extends JPanel{
    private boolean working;
    private JPanel panelFoundGames;
    private List<String> gameIds;

    public GameTrackingPanel(){
        gameIds = new ArrayList<>();
        this.setBackground(Color.BLUE);
        this.setVisible(false);

        panelFoundGames = new JPanel();
        panelFoundGames.setBackground(Color.RED);
        panelFoundGames.setLayout(new BoxLayout(panelFoundGames, BoxLayout.Y_AXIS));
        this.add(panelFoundGames);


        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                working = false;
                GameTrackingPanel.this.setVisible(false);
            }
        });
        this.add(buttonCancel);
    }

    public void startTracking() {
        working = true;
        panelFoundGames.removeAll();
        gameIds.clear();

        long startTime = System.currentTimeMillis() - 600000;
        panelFoundGames.add(new RecentGamePanel(new SimpleDateFormat("HH:mm - dd.MM.yyyy").format(startTime)));
        panelFoundGames.repaint();
        panelFoundGames.revalidate();
        this.setVisible(true);

        LolApiService service = new LolApiService();
        while (working) {
            System.out.println("working");
            List<GameInfo> infoList = service.getRecentRankedGamesInfo();
            for (int i = infoList.size() - 1; i >= 0; i--) {
                GameInfo info = infoList.get(i);
                if (Long.parseLong(info.getDateInMillis()) > startTime) {
                    if (!gameIds.contains(info.getId())){
                        Season.Game game = service.getGameById(info.getId(),true);
                        gameIds.add(info.getId());
                        panelFoundGames.add(new RecentGamePanel(game));
                        panelFoundGames.repaint();
                        panelFoundGames.revalidate();
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
            try {
                if (working){
                    Thread.sleep(5000);
                }
                if (working){
                    Thread.sleep(5000);
                }
                if (working){
                    Thread.sleep(5000);
                }
            } catch (InterruptedException e) {
                working = false;
            }
        }

    }
}
