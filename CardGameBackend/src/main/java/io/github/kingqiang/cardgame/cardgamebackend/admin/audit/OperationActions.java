package io.github.kingqiang.cardgame.cardgamebackend.admin.audit;

/**
 * OperationActions。
 */
public final class OperationActions {

    public static final String USER_BAN = "USER_BAN";
    public static final String USER_UNBAN = "USER_UNBAN";
    public static final String ROOM_DISBAND = "ROOM_DISBAND";
    public static final String ROOM_KICK = "ROOM_KICK";
    public static final String WALLET_ADJUST = "WALLET_ADJUST";
    public static final String CONFIG_UPDATE = "CONFIG_UPDATE";
    public static final String SHOP_ITEM_CREATE = "SHOP_ITEM_CREATE";
    public static final String SHOP_ITEM_UPDATE = "SHOP_ITEM_UPDATE";
    public static final String SHOP_ITEM_DELETE = "SHOP_ITEM_DELETE";
    public static final String ACTIVITY_CREATE = "ACTIVITY_CREATE";
    public static final String ACTIVITY_UPDATE = "ACTIVITY_UPDATE";
    public static final String ACTIVITY_DELETE = "ACTIVITY_DELETE";
    public static final String ROLE_CREATE = "ROLE_CREATE";
    public static final String ROLE_UPDATE = "ROLE_UPDATE";
    public static final String ROLE_DELETE = "ROLE_DELETE";
    public static final String ADMIN_CREATE = "ADMIN_CREATE";
    public static final String ADMIN_UPDATE = "ADMIN_UPDATE";
    public static final String ADMIN_RESET_PASSWORD = "ADMIN_RESET_PASSWORD";
    public static final String EXPORT_DATA = "EXPORT_DATA";

    private OperationActions() {
    }
}
