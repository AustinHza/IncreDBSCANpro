package com.example.dbscan.utils;
import java.util.*;

public class IncDBSCANCluster {


    private final double eps;  // maximum radius of the neighborhood to be considered
    int flag=0;
    private final int minPts;  // minimum number of points needed for a cluster per incrementally update
    static HashSet<Integer>  set;
    public IncDBSCANCluster(final double eps, final int minPts) {
        if (eps < 0.0 || minPts < 1) {
            throw new IllegalArgumentException("DBSCAN param cannot be " +
                    "negative");
        }
        this.eps = eps;
        this.minPts = minPts;
    }

    /**
     * Incrementally update with a new point
     *
     *
     */
    public void incrementalUpdate(Point newPoint, WayPointclass wayPointclass, List<Vessel> vessels, List<Route> routes) {
        wayPointclass.points.add(newPoint);
        // candidates contains q' points.
        List<Point> candidates = new ArrayList<>();
        List<Point> neighbors = getEpsNeighbors(wayPointclass,newPoint);
        for (Point nbr : neighbors) {
            if (nbr == newPoint) {
                // add number of eps-neighbors for new point
                newPoint.epsNbrNum = neighbors.size();
                if (newPoint.epsNbrNum >= minPts) {
                    candidates.add(newPoint);
                    wayPointclass.cores.add(newPoint.pointIndex);
                }
            } else {
                // update number of neighbors.
                nbr.epsNbrNum++;
                // q' is core point in {D union p} but not in D.
                if (nbr.epsNbrNum == minPts) {
                    candidates.add(nbr);
                    wayPointclass.cores.add(nbr.pointIndex);
                }
                //因newpoint加入所满足核心点条件的新增点集合candidates，以及所有的核心点cores
            }
        }

        // find UpdSeed_Ins, q is a core point in {D union p} and
        // q \in N_Eps(q')
        HashSet<Point> updateSeed = new HashSet<>();
        for (Point q_Prime : candidates) {
            List<Point> q_Prime_Neighbors=getEpsNeighbors(wayPointclass,q_Prime);
            for (Point q : q_Prime_Neighbors) {
                if (q.epsNbrNum >= minPts) {
                    updateSeed.add(q);
                }
            }//可能引起更新的核心点
        }


        // different cases based on the UpdSeed_Ins
        if (updateSeed.isEmpty()) {  // UpdSeed is empty, p is a noise point
            System.out.println("Upd is empty");
            newPoint.clusterIndex = Point.NOISE;
        } else {
            // set contains only non-noise cluster index.
            HashSet<Integer> clusterIdSet = new HashSet<>();
            for (Point seed : updateSeed) {
                if (seed.clusterIndex != Point.NOISE) {
                    int rootClusterID = findRootClusterID(wayPointclass,seed.clusterIndex);
                    clusterIdSet.add(rootClusterID);
                }
            }

            if (clusterIdSet.isEmpty()) {
                System.out.println("All seeds are noise");
                // case 1: all seeds were noise before new point insertion,
                // a new cluster containing these noise objects as well as
                // new point is created.
                //clusters.add(new ArrayList<Integer>());
                for (Point seed : updateSeed) {
                    wayPointclass.clusterMapping.put(wayPointclass.clusterGlobalID, wayPointclass.clusterGlobalID);
                    expandCluster(wayPointclass,seed, wayPointclass.clusterGlobalID);
                }
                wayPointclass.clusterGlobalID++;
            } else if (clusterIdSet.size() == 1) {
                System.out.println("All seeds are one cluster");
                // retrieve the unique cluster id.
                int uniqueClusterID = -1;
                for (int id : clusterIdSet) {
                    uniqueClusterID = id;
                }
                // case 2: seeds contain core points of exactly one cluster
                for (Point seed : updateSeed) {
                    expandCluster(wayPointclass,seed, uniqueClusterID);
                }
            } else {
                //若发生合并，需要更新被合并的聚类簇名；vessel对象所维持的wps队列等；route对象也需要及时更新。
                System.out.println("All seeds are different clusters");
                // case 3: seeds contains several clusters, merge these clusters
                //clusters.add(new ArrayList<Integer>());
                newPoint.clusterIndex = wayPointclass.clusterGlobalID;
                for (int id : clusterIdSet) {
                    wayPointclass.clusterMapping.put(id, wayPointclass.clusterGlobalID);
                }
                wayPointclass.clusterMapping.put(wayPointclass.clusterGlobalID, wayPointclass.clusterGlobalID);
                for (Point seed : updateSeed) {
                    expandCluster(wayPointclass,seed, wayPointclass.clusterGlobalID);
                }
                for(Vessel vessel:vessels){
                    for(int id:clusterIdSet){
                        for(int i=0;i<vessel.wps.size();i++){
                            if(vessel.wps.get(i).equals(wayPointclass._class+id))
                                vessel.wps.set(i,wayPointclass._class+wayPointclass.clusterGlobalID);
                        }
                        for(String s:vessel.routes)
                        {
                            if(s.contains(wayPointclass._class+id))
                                s.replace(wayPointclass._class+id,wayPointclass._class+wayPointclass.clusterGlobalID);
                        }
                        HashSet hashSet=new HashSet(vessel.routes);
                        vessel.routes.clear();
                        vessel.routes.addAll(hashSet);
                    }
                }
                for(int i=0;i<routes.size();i++)
                {
                    for(int id:clusterIdSet){
                        if(routes.get(i).r1.equals(wayPointclass._class+id)) {
                            routes.get(i).r1 = wayPointclass._class + wayPointclass.clusterGlobalID;
                            for(int j=0;j<routes.size();j++)
                            {
                                if(routes.get(j).routename.equals(routes.get(i).r1+routes.get(i).r2))
                                {
                                    routes.get(i).params.addAll(routes.get(j).params);
                                    routes.remove(routes.get(j));
                                    i=i>j?i-1:i;
                                    j--;
                                }
                            }
                            routes.get(i).routename=routes.get(i).r1+routes.get(i).r2;
                        }
                        if(routes.get(i).r2.equals(wayPointclass._class+id)) {
                            routes.get(i).r2 = wayPointclass._class + wayPointclass.clusterGlobalID;
                            for(int j=0;j<routes.size();j++)
                            {
                                if(routes.get(j).routename.equals(routes.get(i).r1+routes.get(i).r2)){
                                    routes.get(i).params.addAll(routes.get(j).params);
                                    routes.remove(routes.get(j));
                                    i=i>j?i-1:i;
                                    j--;
                                }
                            }
                            routes.get(i).routename=routes.get(i).r1+routes.get(i).r2;
                        }
                    }
                }
                wayPointclass.clusterGlobalID++;
            }
        }
        newPoint.visited = true;
        countNumClusters(wayPointclass.clusterMapping);//计算聚类簇总数
        //实时更新所有路径关键点的类别
        for(int j=0;j<wayPointclass.points.size();j++)
        {
            if(wayPointclass.points.get(j).clusterIndex!=-1) {
                for (Integer i : set) {
                    if (findRootClusterID(wayPointclass,wayPointclass.points.get(j).clusterIndex) == i)
                    {
                        wayPointclass.points.get(j).clusterIndex=i;
                    }
                }
            }
        }
        //该点是否属于某聚类簇
        if(newPoint.clusterIndex!=-1)
        {
            flag=1;
            newPoint.classed=true;
        }
    }

    /*
     * Get the number of neighbor search operations.
     *
     *
     */

    /*
     * Find root of the tree given a cluster index.
     *
     *
     *
     */
    public  static void countNumClusters(HashMap<Integer, Integer> map) {
        set = new HashSet<>();
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            int flag1=entry.getKey();
            int flag2=entry.getValue();
            if (flag1 == flag2) {
                set.add(entry.getKey());
            }
        }
    }
    private int findRootClusterID(WayPointclass wayPointclass, int id) {
        int root = id;
        if (!wayPointclass.clusterMapping.containsKey(root)) {
            System.out.println("Error: " + root);

        }
        while (wayPointclass.clusterMapping.get(root) != root) {
            root = wayPointclass.clusterMapping.get(root);
            if (!wayPointclass.clusterMapping.containsKey(root)) {
                System.out.println("Error: " + root);
            }
        }

        // path compression
        while (id != root) {
            int parent = wayPointclass.clusterMapping.get(id);
            wayPointclass.clusterMapping.put(id, root);
            id = parent;
        }
        return root;
    }

    /**
     * Expands the cluster to include all density-reachable points.
     * Mark all the density-reachable noise points with the cluster id.
     *
     * @param seed starting core point
     * @param clusterId new cluster id
     */
    private void expandCluster(WayPointclass wayPointclass, Point seed, int clusterId) {
        List<Point> seeds = getEpsNeighbors(wayPointclass,seed);
        int index = 0;
        while (index < seeds.size()) {
            Point current = seeds.get(index);
            // only check noise points
            if (current.clusterIndex == Point.NOISE) {
                current.clusterIndex = clusterId;
                //clusters.get(current.clusterIndex).add(current.label);
                /*List<SinglePoint> currentNeighbors = getEpsNeighbors(current);
                // add noisy density-connected points
                if (currentNeighbors.size() >= minPts) {
                    for (SinglePoint currentNbr : currentNeighbors) {
                        if (currentNbr.clusterIndex == SinglePoint.NOISE) {
                            seeds.add(currentNbr);
                        }
                    }
                }*/
            }
            index++;
        }
    }

    /**
     * Return a list of density-reachable neighbors of a {@code point}
     *
     * @param point the point to look for
     * @return neighbors (including point itself)
     */
    private List<Point> getEpsNeighbors(WayPointclass wayPointclass, Point point) {
        List<Point> neighbors = new ArrayList<>();
        for (Point p : wayPointclass.points) {
            // include point itself
            if (point.haversineDistance(p) <= eps) {
                neighbors.add(p);
            }

        }
        return neighbors;
    }
}