package com.example.dbscan.utils;

public class coordinate {
    static double a = 6378137;
    static double e2 = 0.00669437999013;
    static double e = Math.sqrt(e2);

    // 计算时用到的参数
    // 计算N
    public static double getN(double BB) {
        // BB存放弧度
        double N;
        double sinBB = Math.sin(BB);
        double W = Math.sqrt(1 - e * sinBB * (e * sinBB));
        N = a / W;
        return N;
    }

    // 计算η
    public static double getη(double BB) {
        double η;
        double cos2BB = Math.cos(BB) * Math.cos(BB);
        double ee2 = e2 / (1 - e2);// 第二偏心率
        η = Math.sqrt(ee2 * cos2BB);
        return η;
    }

    // 计算X,子午线弧长
    public static double getX(double BB) {
        // BB存放弧度
        double sinBB = Math.sin(BB);
        double cosBB = Math.cos(BB);

        double X = 0;
        double d = a * (1 - e2);

        double[] ABCDE0 = getABCDE0();
        double A0 = ABCDE0[0];
        double B0 = ABCDE0[1];
        double C0 = ABCDE0[2];
        double D0 = ABCDE0[3];
        double E0 = ABCDE0[4];

        // double sin3BB = sinBB * sinBB * sinBB;
        // double sin5BB = sinBB * sinBB * sin3BB;
        // double sin7BB = sinBB * sinBB * sin5BB;
        double sin2BB = sinBB * sinBB;
        double sin4BB = sin2BB * sin2BB;
        double sin6BB = sin4BB * sin2BB;

        // 计算X
        // X = A0 * BB - cosBB * (B0 * sinBB + C0 * sin3BB + D0 * sin5BB + E0 * sin7BB);
        X = A0 * BB - cosBB * sinBB * (B0 + C0 * sin2BB + D0 * sin4BB + E0 * sin6BB);
        X = X * d;
        return X;
    }

    // 计算A0，B0，C0，D0，E0参数
    public static double[] getABCDE0() {
        double e4 = e2 * e2;
        double e6 = e4 * e2;
        double e8 = e4 * e4;

        double B0 = 3.0 * e2 / 4 + 45.0 * e4 / 64 + 175.0 * e6 / 256 + 11025.0 * e8 / 16384;
        double A0 = B0 + 1;
        double C0 = 15.0 * e4 / 32 + 175.0 * e6 / 384 + 3675.0 * e8 / 8192;
        double D0 = 35.0 * e6 / 96 + 735.0 * e8 / 2048;
        double E0 = 315.0 * e8 / 1024;
        double[] ABCDE0 = new double[5];
        ABCDE0[0] = A0;
        ABCDE0[1] = B0;
        ABCDE0[2] = C0;
        ABCDE0[3] = D0;
        ABCDE0[4] = E0;
        return ABCDE0;

    }

    // degree to radian
    public static double degree2rad(double d) {
        double rad = d * Math.PI / 180;
        return rad;

    }

    // radian to second
    public static float rad2degree(float rad) {
        float d = (float) (rad * 180 / Math.PI);
        return d;
    }

    public static double[] lonlat2xy(double B, double L) {
        // B,L传过来是degree的形式

        // L0中央子午线经度
        double L0 = 123;// 123 degree
        double BB = degree2rad(B);
        double sinBB = Math.sin(BB);
        double cosBB = Math.cos(BB);
        double cos2BB = cosBB * cosBB;

        double N = getN(BB);
        double X = getX(BB);

        double η = getη(BB);
        double η2 = η * η;

        double t = Math.tan(BB);
        double t2 = t * t;

        double l = (L - L0) * Math.PI / 180;
        double l2 = l * l;

        double x = N * sinBB * l2 * cosBB * (0.5 + cos2BB * l2 * ((5 - t2 + 9 * η2 + 4 * η2 * η2) / 24
                + cos2BB * l2 * (61 - 58 * t2 + t2 * t2 + 270 * η2 - 330 * η2 * t2) / 720));
        x = X + x;
        double y = N * l * cosBB
                * (1 + cos2BB * l2 * ((1 - t2 + η2) / 6 + cos2BB * l2 * (5 - 18 * t2 + t2 * t2 + 14 * η2 - 58 * t2 * η2) / 120));

        double[] xy = new double[2];
        xy[0] = x;
        xy[1] = y;
        return xy;
    }

    // 返回的Bf是弧度
    public static double getBf(double x, double y) {

        double[] ABCDE0 = getABCDE0();
        double A0 = ABCDE0[0];
        double B0 = ABCDE0[1];
        double C0 = ABCDE0[2];
        double D0 = ABCDE0[3];
        double E0 = ABCDE0[4];

        double d = a * (1 - e2);

        double Bf0 = x / (d * A0);// 计算低点纬度的初值
        int i = 0;
        while (i < 10000) {

            double sinBf = Math.sin(Bf0);
            double cosBf = Math.cos(Bf0);
            double sin2Bf = sinBf * sinBf;
            double sin4Bf = sin2Bf * sin2Bf;
            double sin6Bf = sin4Bf * sin2Bf;

            double Bf = (x + cosBf * sinBf * d * (B0 + C0 * sin2Bf + D0 * sin4Bf + E0 * sin6Bf)) / (A0 * d);
            if (Math.abs(Bf - Bf0) < 1e-20) {
                return Bf;
            } else {
                Bf0 = Bf;
                i++;
            }
        }
        return Bf0;
    }

    // 计算l,求反算经差
    public static double getl(double y, double t, double η, double N, double Bf)// Bf是弧度
    {
        // l是弧度
        double cosBB = Math.cos(Bf);

        double y2 = y * y;
        double y4 = y2 * y2;

        double t2 = t * t;
        double t4 = t2 * t2;

        double η2 = η * η;

        double N2 = N * N;
        double N4 = N2 * N2;

        double l = y / (N * cosBB)
                * (1.0 - (1 + 2 * t2 + η2) * y2 / (N2 * 6) + (5 + 28 * t2 + 24 * t4 + 6 * η2 + 8 * η2 * t2) * y4 / (N4 * 120));

        return l;
    }

    // 求出的B也是弧度
    public static double getB(double y, double t, double η, double N, double Bf)// Bf是弧度
    {
        // 求出的B也是弧度
        double y2 = y * y;
        double t2 = t * t;
        double t4 = t2 * t2;

        double η2 = η * η;
        double η4 = η2 * η2;

        double N2 = N * N;

        double xiang1 = (1 + η2) / 24;
        double xiang2 = (5 + 3 * t2 + 6 * η2 - 6 * η2 * t2 - 3 * η4 + 9 * η4 * t4) * y2 / N2 / 24;
        double xiang3 = (61 + 90 * t2 + 45 * t4 + 107 * η2 + 162 * η2 * t2 + 45 * η2 * t4) * y2 / N2 * y2 / N2 / 720;

        double B = Bf - t * y2 / N2 * (xiang1 - xiang2 + xiang3);
        return B;
    }

    public static double[] xy2lonlat(double x, double y) {
        double L0 = 123;// degree
        double B = 0;
        double L = 0;
        // L0只是度数，l是弧度
        double Bf = getBf(x, y);// 求出的是弧度的
        if (Bf == -1) {
            B = -1;
            L = -1;
        } else {
            double BB = Bf;
            double t = Math.tan(BB);// t n方法中用的BB rad
            double η = getη(BB);
            double N = getN(BB);

            double l = getl(y, t, η, N, Bf);// l返回的是弧度
            l = l / Math.PI * 180;// degree
            L = L0 + l;// L是角度
            B = getB(y, t, η, N, Bf);// 用的Bf是弧度,return rad
            B = B / Math.PI * 180;// 求出结果后都转化为degree
        }
        double[] lonlat = { B, L };
        return lonlat;
    }

    // public static void main(String[] args) {
    // double[] xy=lonlat2xy(32.57,121.644);
    // System.out.println("x:"+xy[0]+",y:"+xy[1]);
    // double[] bl=xy2lonlat(3605872.0376063227,-127338.71792206193);
    // System.out.println("lat:"+bl[0]+",lon:"+bl[1]);
    // }

}
