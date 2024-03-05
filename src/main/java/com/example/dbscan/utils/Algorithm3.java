package com.example.dbscan.utils;

import java.util.Date;
import java.util.List;

//Route Objects Manager
public class Algorithm3 {
    public void ROM(Vessel vessel, List<Route> routes) {
        if (vessel.wps.size() > 1) {
            String wpa = vessel.wps.get(vessel.wps.size() - 1);
            String wpb = vessel.wps.get(vessel.wps.size() - 2);
            int flag = 0;
            for (Route route : routes) {
                if (route.routename.equals(wpa + wpb))
                    flag = 1;
            }
            if (flag == 0) {
                Route route = new Route(wpa, wpb);
                routes.add(route);
                vessel.routes.add(wpa + wpb);
            }
            Date timestampwpa = vessel.timestampwps.get(0).timestamp;
            Date timestampwpb = vessel.timestampwps.get(1).timestamp;
            for (int i = 0; i < routes.size(); i++) {
                if (routes.get(i).routename.equals(wpa + wpb)) {
                    for (Point track : vessel.tracks) {
                        if ((track.timestamp.getTime() / 1000 - timestampwpb.getTime() / 1000) <= 0 && (track.timestamp.getTime() / 1000 - timestampwpa.getTime() / 1000) >= 0) {
                            routes.get(i).params.add(track);
                        }
                    }
                }
            }
            vessel.timestampwps.remove(0);
        }
    }

}
