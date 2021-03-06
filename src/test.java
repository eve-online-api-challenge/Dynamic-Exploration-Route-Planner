import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class test extends JFrame{
    private JPanel contentPane;
    private JButton authorizeButton;
    private JButton buttonStartRoute;
    private JTextField charID;
    private JTextField authorizationCode;
    private JSpinner Jumps;
    private JButton setCharacterIDButton;
    private JButton EVESSOLoginButton;
    private JTextField refreshTokenTextField;
    private JButton refreshAuthorizationButton;
    private JTextField URLTextField;

    private static long lastAuthRefresh;
    private static long lastUniverseRefresh;

    private final int AUTH_REFRESH_TIME = 1000000; //A little less than 20 minutes to ensure that there's always enough time left in the auth code
    private final int UNIVERSE_REFRESH_TIME = 3600000;

    private List<SolarSystem> visitedSystems = new ArrayList<SolarSystem>();

    private String authCode;
    private int characterID = -1;
    private String websiteCode;
    private String refreshCode;
    private int userID = (int)(Math.random()*1000000);
    private CRESTAuthenticator authenticator = new CRESTAuthenticator(Integer.toString(userID));
    private Universe universe = CSVtoUniverse.generateUniverse();

    public test(){
        setContentPane(contentPane);


// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        setCharacterIDButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                characterID = Integer.parseInt(charID.getText());
            }
        });
        refreshAuthorizationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshCode = refreshTokenTextField.getText();
                authCode = authenticator.refresh(refreshCode);
                lastAuthRefresh = System.currentTimeMillis();
                characterID = CRESTInterface.getCharacterID(authCode);
                charID.setText(Integer.toString(characterID));
            }
        });
        authorizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                websiteCode = authorizationCode.getText();
//                System.out.println(websiteCode);
                String[] codes = authenticator.authorize(websiteCode);
                System.out.println(codes.toString());
                authCode = codes[0];
                refreshCode = codes[1];
                refreshTokenTextField.setText(refreshCode);
                lastAuthRefresh = System.currentTimeMillis();
                characterID = CRESTInterface.getCharacterID(authCode);
                charID.setText(Integer.toString(characterID));
            }
        });
        EVESSOLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                URLTextField.setText(authenticator.start());
            }
        });
        buttonStartRoute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int jumps = Integer.parseInt(Jumps.getValue().toString());
                if(!authCode.isEmpty() && characterID != -1){
                    if(System.currentTimeMillis() - lastUniverseRefresh > UNIVERSE_REFRESH_TIME) {
                        lastUniverseRefresh = System.currentTimeMillis();
                        universe.applyKills(XMLAPIParser.getKills());
                        universe.applyJumps(XMLAPIParser.getJumps());
                    }
                    if(System.currentTimeMillis() - lastAuthRefresh > AUTH_REFRESH_TIME){
                        refreshCode = refreshTokenTextField.getText();
                        authCode = authenticator.refresh(refreshCode);
                        lastAuthRefresh = System.currentTimeMillis();
                    }
                    LinkedList<SolarSystem> route = RouteGenerator.selectBestRoute(RouteGenerator.generateRoutes(universe.getSolarSystem(CRESTInterface.currentLocation(authCode, characterID)), jumps));
                    for(SolarSystem solarSystem : route){
                        solarSystem.setVisited(true, false);
                        System.out.println(solarSystem.toString());
                        CRESTInterface.addWaypoint(authCode, characterID, solarSystem.getID());
                    }
                }
            }
        });
    }

    private void onCancel(){
        dispose();
    }

    public static void main(String[] args) {


        test dialog = new test();
        dialog.pack();
        dialog.setVisible(true);
    }
}
