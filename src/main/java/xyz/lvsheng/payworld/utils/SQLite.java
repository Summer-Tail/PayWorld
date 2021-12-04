package xyz.lvsheng.payworld.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.HashMap;
import java.util.UUID;

public class SQLite {

    //创建+链接数据库 CONNECT

    public static Connection getConnection() throws SQLException {

        SQLiteConfig config = new SQLiteConfig();

        config.setSharedCache(true);

        config.enableRecursiveTriggers(true);

        SQLiteDataSource ds = new SQLiteDataSource(config);

        //⭐你可以命名"jdbc:sqlite:"后面的数据库文件名称，程序运行时若无此文件，会自动创建

        String url = System.getProperty("user.dir"); // 获取工作目录

        ds.setUrl("jdbc:sqlite:" + url + "/plugins/PayWorld/" + "PlayerDataBase.db");

        return ds.getConnection();

    }


    //创建表操作 CREATE TABLE

    public static void createTable(Connection con) throws SQLException {
        StringBuilder worldSQL = new StringBuilder();
        for (World world : Bukkit.getWorlds()) {

            worldSQL.append(", '").append(world.getName()).append("' integer DEFAULT 0");
        }

        String sql = "create table if not exists Player (UUID String " + worldSQL + "); ";

        Statement stat = con.createStatement();

        stat.executeUpdate(sql);

    }

    //新增操作 INSERT

    public static void insert(Connection con, UUID uuid, String worldName, Integer worldTime) throws SQLException {

        String sql = "insert into  Player(UUID,`" + worldName + "`) values(?,?)";

        PreparedStatement pst = con.prepareStatement(sql);

        int idx = 1;

        pst.setString(idx++, uuid.toString());
        pst.setInt(idx, (worldTime < 0 ? 0 : worldTime));

        pst.executeUpdate();


    }


    //修改操作 UPDATE

    public static void update(Connection con, UUID uuid, String worldName, Integer UpdateWorldTime) throws SQLException {

        String sql = "update Player set `" + worldName + "` = ? where UUID = ?";

        PreparedStatement pst = con.prepareStatement(sql);

        int idx = 1;


        pst.setInt(idx++, (UpdateWorldTime < 0 ? 0 : UpdateWorldTime));
        pst.setString(idx, uuid.toString());

        pst.executeUpdate();

    }

    //查找指定世界操作 SELECT

    public static int select(Connection con, UUID uu, String worldName) throws SQLException {

        String sql = "select * from Player where `UUID` = '" + uu.toString() + "'";

        Statement stat = con.createStatement();

        ResultSet rs = stat.executeQuery(sql);

        while (rs.next()) {
            return rs.getInt(worldName);
        }
        return -1;
    }

    //查找全部世界操作

    public static HashMap<String, Integer> select(Connection con, UUID uu) throws SQLException {

        String sql = "select * from Player where `UUID` = '" + uu.toString() + "'";

        Statement stat = con.createStatement();

        ResultSet rs = stat.executeQuery(sql);

        HashMap<String, Integer> map = new HashMap<>();
        while (rs.next()) {
            for (World world : Bukkit.getWorlds()) {
                map.put(world.getName(), rs.getInt(world.getName()));
            }
        }
        return map;
    }


    /**
     * 添加字段
     *
     * @param con
     * @throws SQLException
     */
    public static void carateWorldColumn(Connection con) throws SQLException {
        for (World world : Bukkit.getWorlds()) {
            if (!checkColumnExist(con, world.getName())) {
                String sql = "ALTER TABLE Player ADD COLUMN '" + world.getName() + "' integer DEFAULT 0";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.executeUpdate();
            }
        }
    }


    /**
     * 方法1：检查某表列是否存在
     *
     * @param con
     * @param columnName 列名
     * @return
     */
    private static boolean checkColumnExist(Connection con, String columnName) throws SQLException {

        String sql = "select * from sqlite_master where name='Player' and sql like '%" + columnName + "%'";
        Statement stat = null;

        stat = con.createStatement();
        ResultSet rs = stat.executeQuery(sql);
        if (rs.next()) {
            return rs.getString("sql").contains("'" + columnName + "' integer DEFAULT 0");
        }


        return false;
    }


}