package edu.xzit.core.core.common.constant;

public class ProduceManageDaysArriveConstants {

    private ProduceManageDaysArriveConstants() {
    }

    /**
     *  if (actualPeriod <= 3) {
     *             daysGoodsArrive = "3天内";
     *         } else if (actualPeriod <= 7) {
     *             daysGoodsArrive = "7天内";
     *         } else if (actualPeriod <= 15) {
     *             daysGoodsArrive = "15天内";
     *         } else if (actualPeriod <= 30) {
     *             daysGoodsArrive = "30天内";
     *         } else {
     *             daysGoodsArrive = "30天以上";
     *         }
     */
    public static final String THREE_DAYS = "3天内";
    public static final String SEVEN_DAYS = "7天内";
    public static final String FIFTEEN_DAYS = "15天内";
    public static final String THIRTY_DAYS = "30天内";
    public static final String THIRTY_DAYS_ABOVE = "30天以上";
}
