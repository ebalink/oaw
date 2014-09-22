package es.inteco.rastreador2.dao.rastreo;

import es.inteco.common.Constants;
import es.inteco.common.logging.Logger;
import es.inteco.common.properties.PropertiesManager;
import es.inteco.intav.utils.StringUtils;
import es.inteco.plugin.dao.DataBaseManager;
import es.inteco.rastreador2.actionform.cuentausuario.PeriodicidadForm;
import es.inteco.rastreador2.actionform.observatorio.ResultadoSemillaForm;
import es.inteco.rastreador2.actionform.rastreo.*;
import es.inteco.rastreador2.actionform.semillas.CategoriaForm;
import es.inteco.rastreador2.actionform.semillas.SemillaForm;
import es.inteco.rastreador2.actionform.semillas.UpdateListDataForm;
import es.inteco.rastreador2.dao.cuentausuario.CuentaUsuarioDAO;
import es.inteco.rastreador2.dao.observatorio.ObservatorioDAO;
import es.inteco.rastreador2.dao.semilla.SemillaDAO;
import es.inteco.rastreador2.utils.CrawlerUtils;
import es.inteco.rastreador2.utils.DAOUtils;
import es.inteco.rastreador2.utils.Rastreo;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import static es.inteco.common.Constants.CRAWLER_PROPERTIES;

public final class RastreoDAO {

    private RastreoDAO() {
    }

    public static List<Long> getExecutionObservatoryCrawlerIds(Connection c, Long idObservatoryExecution, long idCategory) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Long> executionObservatoryCrawlersIds = new ArrayList<Long>();

        try {
            String query = "SELECT id FROM rastreos_realizados rr " +
                    "JOIN rastreo r ON (r.id_rastreo = rr.id_rastreo) " +
                    "JOIN lista l ON (l.id_lista = r.semillas) " +
                    "WHERE id_obs_realizado = ?";
            if (idCategory != 0) {
                query = query + " AND l.id_categoria = ? ";
            }
            ps = c.prepareStatement(query);
            ps.setLong(1, idObservatoryExecution);
            if (idCategory != 0) {
                ps.setLong(2, idCategory);
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                executionObservatoryCrawlersIds.add(rs.getLong(1));
            }
            return executionObservatoryCrawlersIds;
        } catch (Exception e) {
            Logger.putLog("Error al cerrar el preparedStament", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
    }

    public static boolean crawlerCanBeThrow(Connection c, String userLogin, int cartuchoID) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = c.prepareStatement("SELECT * FROM usuario u JOIN usuario_cartucho uc ON (u.id_usuario = uc.id_usuario) " +
                    "WHERE u.usuario = ? AND uc.id_cartucho = ?");
            ps.setString(1, userLogin);
            ps.setLong(2, cartuchoID);
            rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (Exception e) {
            Logger.putLog("Error al cerrar el preparedStament", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
        return false;
    }

    public static boolean crawlerToUser(Connection c, Long idCrawling, String userLogin) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = c.prepareStatement("SELECT * FROM usuario u JOIN usuario_cartucho uc ON (u.id_usuario = uc.id_usuario) " +
                    "JOIN cartucho_rastreo cr ON (uc.id_cartucho = cr.id_cartucho) WHERE u.usuario = ? AND cr.id_rastreo = ?");
            ps.setString(1, userLogin);
            ps.setLong(2, idCrawling);
            rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (Exception e) {
            Logger.putLog("Error al cerrar el preparedStament", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
        return false;
    }

    public static boolean crawlerToClientAccount(Connection c, Long idCrawling, String clientLogin) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = c.prepareStatement("SELECT * FROM usuario u JOIN cuenta_cliente_usuario ccu ON (u.id_usuario = ccu.id_usuario) " +
                    "JOIN rastreo r ON (r.id_cuenta = ccu.id_cuenta) " +
                    "WHERE u.usuario = ? AND r.id_rastreo = ?");
            ps.setString(1, clientLogin);
            ps.setLong(2, idCrawling);
            rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (Exception e) {
            Logger.putLog("Error al cerrar el preparedStament", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
        return false;
    }

    public static int countRastreosRealizados(Connection c, Long idCrawling, CargarRastreosRealizadosSearchForm searchForm) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            int count = 1;
            String query = "SELECT COUNT(*) FROM rastreos_realizados rr " +
                    "JOIN usuario u ON (u.id_usuario = rr.id_usuario) WHERE id_rastreo = ?";

            if (searchForm != null) {
                if (searchForm.getInitial_date() != null && !searchForm.getInitial_date().isEmpty()) {
                    query += " AND rr.fecha >= ?";
                }
                if (searchForm.getFinal_date() != null && !searchForm.getFinal_date().isEmpty()) {
                    query += " AND rr.fecha <= ? ";
                }
                if (searchForm.getCartridge() != null && !searchForm.getCartridge().isEmpty()) {
                    query += " AND rr.id_cartucho = ? ";
                }
                if (searchForm.getSeed() != null && !searchForm.getSeed().isEmpty()) {
                    query += " AND rr.id_lista = ? ";
                }
            }

            ps = c.prepareStatement(query);

            ps.setLong(count++, idCrawling);

            if (searchForm != null) {
                if (searchForm.getInitial_date() != null && !searchForm.getInitial_date().isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = sdf.parse(searchForm.getInitial_date());
                    ps.setTimestamp(count++, new Timestamp(date.getTime()));
                }
                if (searchForm.getFinal_date() != null && !searchForm.getFinal_date().isEmpty()) {
                    searchForm.setFinal_date(searchForm.getFinal_date());
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                    Date date = sdf.parse(searchForm.getFinal_date() + " 23:59:59");
                    ps.setTimestamp(count++, new Timestamp(date.getTime()));
                }
                if (searchForm.getCartridge() != null && !searchForm.getCartridge().isEmpty()) {
                    ps.setLong(count++, Long.parseLong(searchForm.getCartridge()));
                }
                if (searchForm.getSeed() != null && !searchForm.getSeed().isEmpty()) {
                    ps.setLong(count, Long.parseLong(searchForm.getSeed()));
                }
            }

            rs = ps.executeQuery();
            int numRes = 0;
            if (rs.next()) {
                numRes = rs.getInt(1);
            }
            return numRes;
        } catch (SQLException e) {
            Logger.putLog("Error al cerrar el preparedStament", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
    }

    public static int countRastreo(Connection c, String user, CargarRastreosSearchForm searchForm) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String query = "SELECT COUNT(*) FROM rastreo r " +
                    "JOIN cartucho_rastreo cr ON r.Id_rastreo = cr.Id_rastreo " +
                    "JOIN cartucho c ON cr.Id_cartucho = c.Id_cartucho " +
                    "JOIN usuario_cartucho uc ON uc.Id_cartucho = c.Id_cartucho " +
                    "JOIN usuario u ON u.Id_usuario = uc.Id_usuario WHERE u.usuario = ? " +
                    "AND id_observatorio IS NULL ";

            int paramCount = 1;

            if (StringUtils.isNotEmpty(searchForm.getName())) {
                query += " AND r.nombre_rastreo like ? ";
            }

            if (StringUtils.isNotEmpty(searchForm.getCartridge())) {
                query += " AND cr.id_cartucho = ? ";
            }

            if (StringUtils.isNotEmpty(searchForm.getActive())) {
                query += " AND activo = ? ";
            }

            ps = c.prepareStatement(query);

            ps.setString(paramCount++, user);

            if (StringUtils.isNotEmpty(searchForm.getName())) {
                ps.setString(paramCount++, "%" + searchForm.getName() + "%");
            }

            if (StringUtils.isNotEmpty(searchForm.getCartridge())) {
                ps.setLong(paramCount++, Long.parseLong(searchForm.getCartridge()));
            }

            if (StringUtils.isNotEmpty(searchForm.getActive())) {
                ps.setLong(paramCount, Long.parseLong(searchForm.getActive()));
            }

            rs = ps.executeQuery();
            int numRes = 0;
            if (rs.next()) {
                numRes = rs.getInt(1);
            }
            return numRes;
        } catch (Exception e) {
            Logger.putLog("Error al cerrar el preparedStament", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
    }

    /*public static String getCrawlingSeedsPath(Connection c, Long idCrawling) throws Exception {
        ResultSet rst = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = c.prepareStatement("SELECT semillas FROM rastreo WHERE id_rastreo = ?");
            pstmt.setLong(1, idCrawling);
            rst = pstmt.executeQuery();

            if (rst.next()) {
                return rst.getString("semillas");
            } else {
                return null;
            }
        } catch (Exception e) {
            Logger.putLog("Error al obtener los datos de la lista de rastreos", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(pstmt, rst);
        }
    }  //*/

    /**
     * Devuelve el listado de rastreos a los que puede acceder un usuario
     *
     * @param c
     * @param user
     * @param searchForm
     * @param pagina
     * @return
     * @throws Exception
     */
    public static CargarRastreosForm getLoadCrawlingForm(Connection c, String user, CargarRastreosSearchForm searchForm, int pagina) throws Exception {
        CargarRastreosForm cargarRastreosForm = new CargarRastreosForm();

        //comprobamos que usuario es y sacamos sus rastreos
        ResultSet rst = null;
        PreparedStatement pstmt = null;
        PropertiesManager pmgr = new PropertiesManager();
        int pagSize = Integer.parseInt(pmgr.getValue(CRAWLER_PROPERTIES, "pagination.size"));
        int resultFrom = pagSize * pagina;
        try {
            int paramCount = 1;
            String query = "SELECT * FROM rastreo r JOIN cartucho_rastreo cr ON r.Id_rastreo = cr.Id_rastreo " +
                    "JOIN cartucho c ON cr.Id_cartucho = c.Id_cartucho " +
                    "JOIN usuario_cartucho uc ON uc.Id_cartucho = c.Id_cartucho " +
                    "JOIN usuario u ON u.Id_usuario = uc.Id_usuario " +
                    "WHERE u.usuario = ? AND id_observatorio IS NULL ";

            if (StringUtils.isNotEmpty(searchForm.getName())) {
                query += " AND r.nombre_rastreo like ? ";
            }

            if (StringUtils.isNotEmpty(searchForm.getCartridge())) {
                query += " AND cr.id_cartucho = ? ";
            }

            if (StringUtils.isNotEmpty(searchForm.getActive())) {
                query += " AND activo = ? ";
            }

            query += "ORDER BY r.fecha_lanzado DESC LIMIT ? OFFSET ?";
            pstmt = c.prepareStatement(query);

            pstmt.setString(paramCount++, user);

            if (StringUtils.isNotEmpty(searchForm.getName())) {
                pstmt.setString(paramCount++, "%" + searchForm.getName() + "%");
            }

            if (StringUtils.isNotEmpty(searchForm.getCartridge())) {
                pstmt.setLong(paramCount++, Long.parseLong(searchForm.getCartridge()));
            }

            if (StringUtils.isNotEmpty(searchForm.getActive())) {
                pstmt.setLong(paramCount++, Long.parseLong(searchForm.getActive()));
            }

            pstmt.setInt(paramCount++, pagSize);
            pstmt.setInt(paramCount, resultFrom);
            rst = pstmt.executeQuery();
            int num_rastreos = 0;
            boolean firstIteration = true;
            final DateFormat df = new SimpleDateFormat(pmgr.getValue(CRAWLER_PROPERTIES, "date.format.simple"));
            final List<Rastreo> rastreos = new ArrayList<Rastreo>();
            while (rst.next()) {
                if (firstIteration) {
                    cargarRastreosForm.setMaxrastreos(rst.getInt("numrastreos"));
                    cargarRastreosForm.setCartucho(rst.getString("aplicacion"));
                    firstIteration = false;
                }
                num_rastreos++;
                Rastreo r = new Rastreo();
                int id_rastreo = rst.getInt("id_rastreo");
                //Código del rastreo
                String nombreRastreo = rst.getString("nombre_rastreo");
                if (nombreRastreo.contains("-")) {
                    nombreRastreo = nombreRastreo.substring(0, nombreRastreo.indexOf('-'));
                }

                if (nombreRastreo.length() > Integer.parseInt(pmgr.getValue(CRAWLER_PROPERTIES, "break.characters.table.string"))) {
                    r.setCodigo(nombreRastreo.substring(0, Integer.parseInt(pmgr.getValue(CRAWLER_PROPERTIES, "break.characters.table.string"))) + "...");
                    r.setCodigoTitle(nombreRastreo);
                } else {
                    r.setCodigo(nombreRastreo);
                }
                r.setActivo(rst.getLong("activo"));
                r.setId_rastreo(String.valueOf(id_rastreo));
                r.setPseudoAleatorio(String.valueOf(rst.getBoolean("pseudoaleatorio")));
                r.setProfundidad(rst.getInt("profundidad"));
                //Fecha del rastreo
                Date date = rst.getDate("fecha");
                if (date != null) {
                    r.setFecha(df.format(date));
                }
                r.setCartucho(rst.getString("aplicacion"));

                //Obtenemos el estado del rastreo
                //1: NO LANZADO
                //2: LANZADO
                //3: PARADO
                //4: TERMINADO
                //int est = -1;
                r.setEstado(rst.getInt("estado"));
                r.setEstadoTexto("rastreo.estado." + r.getEstado());
                r.setId_cuenta(rst.getLong("id_cuenta"));
                rastreos.add(r);
            }

            cargarRastreosForm.setVr(rastreos);
            cargarRastreosForm.setNum_rastreos(num_rastreos);
        } catch (Exception e) {
            Logger.putLog("Error al obtener los datos de la lista de rastreos", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(pstmt, rst);
        }

        return cargarRastreosForm;
    }

    public static boolean existAccountFromCrawler(Connection c, long id_rastreo) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = c.prepareStatement("SELECT * FROM rastreo WHERE id_rastreo = ?");
            ps.setLong(1, id_rastreo);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("id_cuenta") != 0;
            }
        } catch (Exception e) {
            Logger.putLog("Error: ", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
        return false;
    }

    public static long getIdLRFromRastreo(Connection c, long id_rastreo) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = c.prepareStatement("SELECT lista_rastreable FROM rastreo WHERE id_rastreo = ? ");
            ps.setLong(1, id_rastreo);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getLong("lista_rastreable");
            }

        } catch (Exception e) {
            Logger.putLog("Error: ", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
        return 0;
    }

    public static long getIdLNRFromRastreo(Connection c, long id_rastreo) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = c.prepareStatement("SELECT lista_no_rastreable FROM rastreo WHERE id_rastreo = ? ");
            ps.setLong(1, id_rastreo);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getLong("lista_no_rastreable");
            }

        } catch (Exception e) {
            Logger.putLog("Error: ", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
        return 0;
    }

    public static void borrarRastreo(Connection c, long id_rastreo) throws Exception {
        final List<Connection> connections = DAOUtils.getCartridgeConnections();

        try {
            c.setAutoCommit(false);
            for (Connection conn : connections) {
                conn.setAutoCommit(false);
            }

            borrarRastreo(c, connections, id_rastreo);
            c.commit();
            for (Connection conn : connections) {
                conn.commit();
            }
        } catch (Exception e) {
            try {
                c.rollback();
            } catch (SQLException e1) {
                Logger.putLog("Exception: ", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
                throw e;
            }
            for (Connection conn : connections) {
                try {
                    conn.rollback();
                } catch (Exception excep) {
                    Logger.putLog("Exception: ", CuentaUsuarioDAO.class, Logger.LOG_LEVEL_ERROR, e);
                    throw e;
                }
            }
            throw e;
        } finally {
            for (Connection conn : connections) {
                DataBaseManager.closeConnection(conn);
            }
        }
    }

    public static void borrarRastreo(Connection c, List<Connection> connections, long id_rastreo) throws Exception {
        PreparedStatement ps = null;
        try {
            boolean existAccount = existAccountFromCrawler(c, id_rastreo);
            long idListaRastreable = getIdLRFromRastreo(c, id_rastreo);
            long idListaNoRastreable = getIdLNRFromRastreo(c, id_rastreo);
            List<Long> executedCrawlingIdsList = getExecutedCrawlerIds(c, id_rastreo);
            ps = c.prepareStatement("DELETE FROM rastreo WHERE id_rastreo = ?");
            ps.setLong(1, id_rastreo);
            ps.executeUpdate();
            DAOUtils.closeQueries(ps, null);
            if (!existAccount) {
                if (idListaRastreable != 0) {
                    ps = c.prepareStatement("DELETE FROM lista WHERE id_lista = ?");
                    ps.setLong(1, idListaRastreable);
                    ps.executeUpdate();
                    DAOUtils.closeQueries(ps, null);
                }
                if (idListaNoRastreable != 0) {
                    ps = c.prepareStatement("DELETE FROM lista WHERE id_lista = ?");
                    ps.setLong(1, idListaNoRastreable);
                    ps.executeUpdate();
                    DAOUtils.closeQueries(ps, null);
                }
            }
            if (executedCrawlingIdsList != null && !executedCrawlingIdsList.isEmpty()) {
                deleteAnalyse(connections, executedCrawlingIdsList);
            }
        } catch (SQLException e) {
            Logger.putLog("Error al cerrar el preparedStament", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, null);
        }
    }

    public static void borrarRastreoRealizado(Connection c, long id_rastreo_realizado) throws Exception {
        final List<Connection> connections = DAOUtils.getCartridgeConnections();

        try {
            c.setAutoCommit(false);
            for (Connection conn : connections) {
                conn.setAutoCommit(false);
            }

            borrarRastreoRealizado(c, connections, id_rastreo_realizado);
            c.commit();
            for (Connection conn : connections) {
                conn.commit();
            }
        } catch (Exception e) {
            try {
                c.rollback();
            } catch (SQLException e1) {
                Logger.putLog("Exception: ", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
                throw e;
            }
            for (Connection conn : connections) {
                try {
                    conn.rollback();
                } catch (Exception excep) {
                    Logger.putLog("Exception: ", CuentaUsuarioDAO.class, Logger.LOG_LEVEL_ERROR, e);
                    throw e;
                }
            }
            throw e;
        } finally {
            for (Connection conn : connections) {
                DataBaseManager.closeConnection(conn);
            }
        }
    }

    public static void borrarRastreoRealizado(Connection c, List<Connection> connections, long id_rastreo_realizado) throws Exception {
        PreparedStatement ps = null;
        try {
            ps = c.prepareStatement("DELETE FROM rastreos_realizados WHERE id = ?");
            ps.setLong(1, id_rastreo_realizado);
            ps.executeUpdate();
            List<Long> executedCrawlingIdsList = new ArrayList<Long>();
            executedCrawlingIdsList.add(id_rastreo_realizado);
            deleteAnalyse(connections, executedCrawlingIdsList);
        } catch (SQLException e) {
            Logger.putLog("Error al cerrar el preparedStament", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, null);
        }
    }

    public static List<Long> getExecutedCrawlerIds(Connection connR, long id_rastreo) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Long> executedCrawlerIds = new ArrayList<Long>();
        try {
            //RECUPERAMOS LOS IDS DE LOS RASTREOS REALIZADOS
            ps = connR.prepareStatement("SELECT rr.id FROM rastreo r " +
                    "JOIN rastreos_realizados rr ON (r.id_rastreo = rr.id_rastreo) " +
                    "WHERE r.id_rastreo = ?");
            ps.setLong(1, id_rastreo);
            rs = ps.executeQuery();
            while (rs.next()) {
                executedCrawlerIds.add(rs.getLong("id"));
            }
            return executedCrawlerIds;
        } catch (Exception e) {
            Logger.putLog("Exception: ", ObservatorioDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
    }

    public static Long getExecutedCrawlerId(Connection connR, long idRastreo, long idExecutedObservatory) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            //RECUPERAMOS LOS IDS DE LOS RASTREOS REALIZADOS
            ps = connR.prepareStatement("SELECT rr.id FROM rastreo r " +
                    "JOIN rastreos_realizados rr ON (r.id_rastreo = rr.id_rastreo) " +
                    "WHERE r.id_rastreo = ? AND rr.id_obs_realizado = ?");
            ps.setLong(1, idRastreo);
            ps.setLong(2, idExecutedObservatory);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("id");
            }
            return null;
        } catch (Exception e) {
            Logger.putLog("Exception: ", ObservatorioDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
    }

    public static List<Long> getEvolutionExecutedCrawlerIds(Connection connR, long id_rastreo, long idTracking, long id_cartucho) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Long> executedCrawlerIds = new ArrayList<Long>();
        PropertiesManager pmgr = new PropertiesManager();
        try {
            ps = connR.prepareStatement("SELECT * FROM rastreos_realizados r WHERE id_rastreo = ? AND " +
                    "fecha <= (SELECT fecha FROM rastreos_realizados rr WHERE id = ?) AND " +
                    "id_cartucho = ? " +
                    "ORDER BY fecha DESC LIMIT ?");
            ps.setLong(1, id_rastreo);
            ps.setLong(2, idTracking);
            ps.setLong(3, id_cartucho);
            ps.setLong(4, Long.parseLong(pmgr.getValue(CRAWLER_PROPERTIES, "intav.evolution.limit")));

            rs = ps.executeQuery();
            while (rs.next()) {
                executedCrawlerIds.add(rs.getLong("id"));
            }
            return executedCrawlerIds;
        } catch (Exception e) {
            Logger.putLog("Exception: ", ObservatorioDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
    }

    public static List<Long> getExecutedCrawlerClientAccountsIds(Connection connR, long idClientAccount) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Long> executedCrawlerIds = new ArrayList<Long>();
        try {
            //RECUPERAMOS LOS IDS DE LOS RASTREOS REALIZADOS
            ps = connR.prepareStatement("SELECT id FROM rastreos_realizados rr " +
                    "JOIN rastreo r ON (rr.id_rastreo = r.id_rastreo) WHERE r.id_cuenta = ?");
            ps.setLong(1, idClientAccount);
            rs = ps.executeQuery();
            while (rs.next()) {
                executedCrawlerIds.add(rs.getLong("id"));
            }
            return executedCrawlerIds;
        } catch (SQLException sqle) {
            Logger.putLog("Exception: ", ObservatorioDAO.class, Logger.LOG_LEVEL_ERROR, sqle);
            throw sqle;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
    }

    public static void deleteAnalyse(List<Connection> connections, List<Long> idsRastreosRealizados) throws SQLException {
        if (idsRastreosRealizados != null && !idsRastreosRealizados.isEmpty()) {
            PreparedStatement ps = null;

            try {
                //ELIMINAMOS LOS ANÁLISIS E INCIDENCIAS DE  LOS RASTREOS DEL OBSERVATORIO
                StringBuilder executionIdStrList = new StringBuilder("(");
                for (int i = 1; i <= idsRastreosRealizados.size(); i++) {
                    if (idsRastreosRealizados.size() > i) {
                        executionIdStrList.append("?,");
                    } else if (idsRastreosRealizados.size() == i) {
                        executionIdStrList.append("?)");
                    }
                }
                for (Connection conn : connections) {
                    if (conn.getCatalog().contains("intav")) {
                        ps = conn.prepareStatement("DELETE FROM tanalisis WHERE cod_rastreo IN " + executionIdStrList);
                    } else if (conn.getCatalog().contains("lenox")) {
                        ps = conn.prepareStatement("DELETE FROM SEXISTA_RASTREOS WHERE id_rastreo IN " + executionIdStrList);
                    } else if (conn.getCatalog().contains("malware")) {
                        ps = conn.prepareStatement("DELETE FROM a001_malware_resultados WHERE id_rastreo IN " + executionIdStrList);
                    } else if (conn.getCatalog().contains("multilanguage")) {
                        ps = conn.prepareStatement("DELETE FROM analysis WHERE id_crawling IN " + executionIdStrList);
                    }

                    if (ps != null) {
                        for (int i = 0; i < idsRastreosRealizados.size(); i++) {
                            ps.setLong(i + 1, idsRastreosRealizados.get(i));
                        }
                        ps.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                Logger.putLog("Exception: ", ObservatorioDAO.class, Logger.LOG_LEVEL_ERROR, e);
                throw e;
            } finally {
                DAOUtils.closeQueries(ps, null);
            }
        }
    }

    /*public static void borrarRastreos(String id_cartucho, Connection c) throws SQLException {

        PreparedStatement pstmt = null;
        ResultSet rst = null;
        try {
            pstmt = c.prepareStatement("DELETE FROM cartucho_rastreo WHERE Id_Cartucho=?;");
            pstmt.setString(1, id_cartucho);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Logger.putLog("Error al cerrar el preparedStament", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(pstmt, rst);
        }
    }*/

    public static DatosCartuchoRastreoForm cargarDatosCartuchoRastreo(Connection c, String nombreRastreo) throws SQLException {

        DatosCartuchoRastreoForm datosCartuchoRastreoForm = new DatosCartuchoRastreoForm();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sq = "SELECT r.*, lg.*, c.id_cartucho, c.nombre as nombre_cartucho, c.numrastreos, ll.acronimo " +
                    "FROM rastreo r INNER JOIN cartucho_rastreo cr ON r.id_rastreo = cr.id_rastreo " +
                    "INNER JOIN cartucho c  ON c.id_cartucho = cr.id_cartucho " +
                    "JOIN languages lg ON r.id_language = lg.id_language " +
                    "JOIN lista ll ON (r.semillas = ll.id_lista) " +
                    "WHERE r.id_rastreo = ?";
            ps = c.prepareStatement(sq);
            ps.setString(1, nombreRastreo);
            rs = ps.executeQuery();
            while (rs.next()) {
                datosCartuchoRastreoForm.setNombreRastreo(rs.getString("nombre_rastreo"));
                datosCartuchoRastreoForm.setId_rastreo(rs.getInt("id_rastreo"));
                datosCartuchoRastreoForm.setId_cartucho(rs.getInt("id_cartucho"));
                datosCartuchoRastreoForm.setNombre_cart(rs.getString("nombre_cartucho"));
                datosCartuchoRastreoForm.setNumRastreos(rs.getInt("numrastreos"));
                datosCartuchoRastreoForm.setProfundidad(rs.getInt("profundidad"));
                datosCartuchoRastreoForm.setTopN(rs.getInt("topn"));
                String nomRastreo = rs.getString("nombre_rastreo");
                if (nomRastreo.contains("-")) {
                    nomRastreo = nomRastreo.substring(0, nomRastreo.indexOf('-'));
                }
                datosCartuchoRastreoForm.setNombre_rastreo(nomRastreo);
                datosCartuchoRastreoForm.setIdSemilla(rs.getLong("semillas"));
                datosCartuchoRastreoForm.setSeedAcronym(rs.getString("acronimo"));
                datosCartuchoRastreoForm.setListaNoRastreable(rs.getString("lista_no_rastreable"));
                datosCartuchoRastreoForm.setListaRastreable(rs.getString("lista_rastreable"));
                datosCartuchoRastreoForm.setIdCuentaCliente(rs.getLong("id_cuenta"));
                datosCartuchoRastreoForm.setIdObservatory(rs.getLong("id_observatorio"));
                datosCartuchoRastreoForm.setPseudoaleatorio(rs.getBoolean("pseudoaleatorio"));
                datosCartuchoRastreoForm.setExhaustive(rs.getBoolean("exhaustive"));
                datosCartuchoRastreoForm.setInDirectory(rs.getBoolean("in_directory"));

                LenguajeForm lenguajeForm = new LenguajeForm();
                lenguajeForm.setId(rs.getLong("r.id_language"));
                lenguajeForm.setKeyName(rs.getString("key_name"));
                lenguajeForm.setCodice(rs.getString("codice"));
                datosCartuchoRastreoForm.setLanguage(lenguajeForm);
            }
        } catch (SQLException e) {
            Logger.putLog("Exception", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);

        }
        return datosCartuchoRastreoForm;
    }

    public static void updateRastreo(Connection c, long id_rast) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = c.prepareStatement("UPDATE rastreo SET fecha_lanzado = ? WHERE id_rastreo = ?");
            pst.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            pst.setLong(2, id_rast);
            pst.executeUpdate();
        } catch (SQLException e) {
            Logger.putLog("Exception", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(pst, null);

        }
    }

    public static void actualizarEstadoRastreo(Connection c, int id_rast, int status) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = c.prepareStatement("UPDATE rastreo SET estado = ? WHERE id_rastreo = ?");
            pst.setInt(1, status);
            pst.setInt(2, id_rast);
            pst.executeUpdate();
        } catch (SQLException e) {
            Logger.putLog("Exception", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(pst, null);

        }
    }

    public static String cargarFechaRastreo(Connection c, String nombreRastreo) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String f = "";
        try {
            ps = c.prepareStatement("SELECT * FROM rastreo WHERE nombre_rastreo = ?");
            ps.setString(1, nombreRastreo);
            rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    if (rs.getString("fecha_lanzado") == null) {
                        f = "No Lanzado";
                    } else {
                        f = rs.getString("fecha_lanzado");
                    }
                } catch (Exception e) {
                    f = "No Lanzado";
                }
            }
        } catch (SQLException e) {
            Logger.putLog("Exception", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);

        }
        return f;
    }

    public static boolean existeRastreo(Connection c, String nombreRastreo) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = c.prepareStatement("SELECT * FROM rastreo WHERE nombre_rastreo = ?");
            ps.setString(1, nombreRastreo);
            rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            Logger.putLog("Exception", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);

        }
        return false;
    }

    public static String recuperarNombreRastreo(Connection c, Long idCrawling) throws Exception {
        PreparedStatement pes = null;
        ResultSet res = null;
        try {
            pes = c.prepareStatement("SELECT * FROM rastreo WHERE id_rastreo = ?;");
            pes.setLong(1, idCrawling);
            res = pes.executeQuery();
            if (res.next()) {
                return res.getString("nombre_rastreo");
            }
        } catch (Exception e) {
            Logger.putLog("Error al cerrar el preparedStament", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            try {
                if (pes != null) {
                    pes.close();
                }
                if (res != null) {
                    res.close();
                }
            } catch (Exception e) {
                Logger.putLog("Error al cerrar el preparedStament", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            }
        }
        return null;
    }

    public static InsertarRastreoForm cargarRastreo(Connection c, int idRastreo, InsertarRastreoForm rastreo) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        PropertiesManager pmgr = new PropertiesManager();
        try {
            ps = c.prepareStatement("SELECT r.*, l.nombre AS nombreSemilla, l1.lista AS listaRastreable, " +
                    "l2.lista AS listaNoRastreable, cr.id_cartucho FROM rastreo r " +
                    "JOIN cartucho_rastreo cr ON (r.id_rastreo = cr.id_rastreo) " +
                    "LEFT JOIN lista l ON (l.id_lista = r.semillas)" +
                    "LEFT JOIN lista l1 ON (l1.id_lista = r.lista_rastreable)" +
                    "LEFT JOIN lista l2 ON (l2.id_lista = r.lista_no_rastreable)" +
                    "WHERE r.id_rastreo = ?");
            ps.setInt(1, idRastreo);
            rs = ps.executeQuery();
            if (rs.next()) {
                //Código del rastreo
                String nombreRastreo = rs.getString("nombre_rastreo");
                if (nombreRastreo.contains("-")) {
                    nombreRastreo = nombreRastreo.substring(0, nombreRastreo.indexOf('-'));
                }

                rastreo.setCuenta_cliente(rs.getLong("id_cuenta"));
                rastreo.setNormaAnalisis(rs.getString("id_guideline"));
                rastreo.setLenguaje(rs.getLong("id_language"));
                rastreo.setCodigo(nombreRastreo);
                DateFormat dateFormat = new SimpleDateFormat(pmgr.getValue(CRAWLER_PROPERTIES, "date.format.simple"));
                rastreo.setFecha(dateFormat.format(rs.getDate("Fecha")));
                rastreo.setProfundidad(rs.getInt("profundidad"));
                rastreo.setTopN(rs.getInt("topn"));
                rastreo.setSemilla(rs.getString("nombreSemilla"));
                rastreo.setId_semilla(rs.getLong("semillas"));
                rastreo.setId_lista_no_rastreable(rs.getLong("lista_no_rastreable"));
                rastreo.setId_lista_rastreable(rs.getLong("lista_rastreable"));
                rastreo.setListaNoRastreable(rs.getString("listaNoRastreable"));
                rastreo.setListaRastreable(rs.getString("listaRastreable"));
                rastreo.setCuenta_cliente(rs.getLong("id_cuenta"));
                rastreo.setCartucho(rs.getString("id_cartucho"));
                rastreo.setExhaustive(rs.getBoolean("exhaustive"));
                rastreo.setPseudoAleatorio(rs.getBoolean("pseudoaleatorio"));
            }
        } catch (SQLException e) {
            Logger.putLog("Exception", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
        return rastreo;
    }

    public static VerRastreoForm cargarRastreoVer(Connection c, long idRastreo, VerRastreoForm rastreo) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        rastreo.setId_rastreo(idRastreo);
        try {
            ps = c.prepareStatement("SELECT * , l.lista AS semilla, l1.lista AS l_lista_rastreable, " +
                    "l2.lista AS l_lista_no_rastreable, cc.nombre AS cuentaCliente, c.nombre AS nombreCartucho " +
                    "FROM rastreo r " +
                    "JOIN cartucho_rastreo cr ON (r.id_rastreo = cr.id_rastreo) " +
                    "JOIN cartucho c ON (cr.id_cartucho = c.id_cartucho) " +
                    "LEFT JOIN cuenta_cliente cc ON (r.id_cuenta = cc.id_cuenta) " +
                    "LEFT JOIN lista l ON (l.id_lista = r.semillas) " +
                    "LEFT JOIN lista l1 ON (l1.id_lista = r.lista_rastreable) " +
                    "LEFT JOIN lista l2 ON (l2.id_lista = r.lista_no_rastreable) " +
                    "WHERE r.Id_Rastreo = ?");
            ps.setLong(1, idRastreo);
            rs = ps.executeQuery();
            while (rs.next()) {
                String nombreRastreo = rs.getString("nombre_rastreo");
                if (nombreRastreo.contains("-")) {
                    nombreRastreo = nombreRastreo.substring(0, nombreRastreo.indexOf('-'));
                }
                rastreo.setRastreo(nombreRastreo);
                rastreo.setFecha(rs.getString(3));
                if (rastreo.getFecha().endsWith(".0")) {
                    rastreo.setFecha(rastreo.getFecha().substring(0, rastreo.getFecha().length() - 2));
                }
                if (rs.getString("semilla") != null) {
                    rastreo.setUrl_semilla(convertStringToList(rs.getString("semilla")));
                }
                if (rs.getString("lista_rastreable") != null) {
                    rastreo.setUrl_listaRastreable(convertStringToList(rs.getString("l_lista_rastreable")));
                }
                if (rs.getString("lista_no_rastreable") != null) {
                    rastreo.setUrl_listaNoRastreable(convertStringToList(rs.getString("l_lista_no_rastreable")));
                }
                rastreo.setProfundidad(rs.getInt(5));
                rastreo.setTopN_ver(rs.getString(6));
                rastreo.setPseudoAleatorio(String.valueOf(rs.getBoolean("pseudoaleatorio")));
                rastreo.setNormaAnalisis(rs.getLong("id_guideline"));
                rastreo.setAutomatico(rs.getInt("automatico"));
                rastreo.setActivo(rs.getLong("activo"));
                rastreo.setInDirectory(rs.getBoolean("in_directory"));
                try {
                    if (rs.getString("fecha_lanzado") != null) {
                        rastreo.setFechaLanzado(rs.getString("fecha_lanzado"));
                        if (rastreo.getFechaLanzado().endsWith(".0")) {
                            rastreo.setFechaLanzado(rastreo.getFechaLanzado().substring(0, rastreo.getFechaLanzado().length() - 2));
                        }
                    }
                } catch (Exception e) {
                    // Logger.putLog("Error al añadir un rastreo realizado ", RastreoDAO.class, Logger.LOG_LEVEL_INFO);
                }

                rastreo.setListaNoRastreable(rs.getString("lista_no_rastreable"));
                rastreo.setListaRastreable(rs.getString("lista_rastreable"));

                rastreo.setNombre_cartucho(rs.getString("nombrecartucho"));
                rastreo.setId_cartucho(rs.getInt("id_cartucho"));
                rastreo.setCuentaCliente(rs.getString("cuentacliente"));
            }
        } catch (SQLException e) {
            Logger.putLog("Exception", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
        return rastreo;
    }

    public static RastreoEjecutadoForm cargarRastreoEjecutado(Connection c, long idExecution) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        RastreoEjecutadoForm rastreo = new RastreoEjecutadoForm();

        try {
            ps = c.prepareStatement("SELECT r.*, rr.* FROM rastreo r " +
                    "JOIN rastreos_realizados rr ON (r.id_rastreo = rr.id_rastreo) " +
                    "WHERE rr.id = ? ");
            ps.setLong(1, idExecution);
            rs = ps.executeQuery();
            while (rs.next()) {
                rastreo.setId_ejecucion(rs.getLong("id"));
                rastreo.setId_rastreo(rs.getLong("id_rastreo"));
                rastreo.setFecha(CrawlerUtils.formatDate(rs.getDate("rr.fecha")));
                rastreo.setNombre_rastreo(rs.getString("nombre_rastreo"));
                rastreo.setId_cartucho(rs.getLong("rr.id_cartucho"));
            }
        } catch (SQLException e) {
            Logger.putLog("Exception", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
        return rastreo;
    }


    public static boolean rastreoValidoParaUsuario(Connection c, int id_rastreo, String user) throws SQLException {
        //comprobamos que el rastreo es valido para este usuario
        PreparedStatement pstmt = null;
        ResultSet rst = null;
        try {
            pstmt = c.prepareStatement("SELECT COUNT(*) FROM cartucho_rastreo WHERE id_rastreo = ? and id_cartucho = (SELECT id_cartucho FROM usuario WHERE usuario = ?)");
            pstmt.setInt(1, id_rastreo);
            pstmt.setString(2, user);
            rst = pstmt.executeQuery();
            if (rst.next()) {
                return rst.getInt(1) != 0;
            }
        } catch (SQLException e) {
            Logger.putLog("Exception", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(pstmt, rst);

        }
        return true;
    }

    public static String insertarRastreo(Connection c, InsertarRastreoForm insertarRastreoForm, boolean isAutomatic) throws Exception {
        PropertiesManager pmgr = new PropertiesManager();
        PreparedStatement ps = null;
        ResultSet rs = null;
        int id_cartucho = -1;
        try {
            c.setAutoCommit(false);

            ps = c.prepareStatement("SELECT * FROM cartucho WHERE id_cartucho = ?");
            ps.setString(1, insertarRastreoForm.getCartucho());
            rs = ps.executeQuery();
            while (rs.next()) {
                id_cartucho = rs.getInt(1);
            }
            DAOUtils.closeQueries(ps, rs);
            insertarRastreoForm.setId_cartucho(id_cartucho);
            if (id_cartucho != Integer.parseInt(pmgr.getValue(CRAWLER_PROPERTIES, "cartridge.intav.id"))) {
                ps = c.prepareStatement("INSERT INTO rastreo (nombre_rastreo, fecha, profundidad, topn, semillas, lista_no_rastreable, lista_rastreable, estado, id_cuenta, pseudoaleatorio, activo, id_language, id_observatorio, automatico, exhaustive, in_directory) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            } else {
                ps = c.prepareStatement("INSERT INTO rastreo (nombre_rastreo, fecha, profundidad, topn, semillas, lista_no_rastreable, lista_rastreable, estado, id_cuenta, pseudoaleatorio, activo, id_language, id_observatorio, automatico, exhaustive, in_directory, id_guideline) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                if (insertarRastreoForm.getNormaAnalisisEnlaces() != null && insertarRastreoForm.getNormaAnalisisEnlaces().equals("1")) {
                    if (insertarRastreoForm.getNormaAnalisis().equals(pmgr.getValue(CRAWLER_PROPERTIES, "cartridge.une.intav.id"))) {
                        ps.setString(17, pmgr.getValue(CRAWLER_PROPERTIES, "cartridge.une.intav.aux.id"));
                    } else if (insertarRastreoForm.getNormaAnalisis().equals(pmgr.getValue(CRAWLER_PROPERTIES, "cartridge.wcag1.intav.id"))) {
                        ps.setString(17, pmgr.getValue(CRAWLER_PROPERTIES, "cartridge.wcag1.intav.aux.id"));
                    } else {
                        ps.setString(17, insertarRastreoForm.getNormaAnalisis());
                    }

                } else {
                    ps.setString(17, insertarRastreoForm.getNormaAnalisis());
                }
            }
            ps.setString(1, insertarRastreoForm.getCodigo());
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, insertarRastreoForm.getProfundidad());
            ps.setLong(4, insertarRastreoForm.getTopN());
            ps.setLong(5, insertarRastreoForm.getId_semilla());
            if (insertarRastreoForm.getId_lista_no_rastreable() != 0) {
                ps.setLong(6, insertarRastreoForm.getId_lista_no_rastreable());
            } else {
                ps.setString(6, null);
            }
            if (insertarRastreoForm.getId_lista_rastreable() != 0) {
                ps.setLong(7, insertarRastreoForm.getId_lista_rastreable());
            } else {
                ps.setString(7, null);
            }
            ps.setInt(8, Constants.STATUS_NOT_LAUNCHED);
            if (insertarRastreoForm.getCuenta_cliente() != null) {
                ps.setLong(9, insertarRastreoForm.getCuenta_cliente());
            } else {
                ps.setNull(9, Types.BIGINT);
            }
            ps.setBoolean(10, insertarRastreoForm.isPseudoAleatorio());
            ps.setBoolean(11, insertarRastreoForm.isActive());
            ps.setLong(12, insertarRastreoForm.getLenguaje());
            if (insertarRastreoForm.getId_observatorio() != null) {
                ps.setLong(13, insertarRastreoForm.getId_observatorio());
            } else {
                ps.setString(13, null);
            }
            ps.setBoolean(14, isAutomatic);
            ps.setBoolean(15, insertarRastreoForm.isExhaustive());
            ps.setBoolean(16, insertarRastreoForm.isInDirectory());
            ps.executeUpdate();
            DAOUtils.closeQueries(ps, rs);

            int id_rastreo = -1;
            ps = c.prepareStatement("SELECT * FROM rastreo WHERE nombre_rastreo = ?");
            ps.setString(1, insertarRastreoForm.getCodigo());
            rs = ps.executeQuery();
            while (rs.next()) {
                id_rastreo = rs.getInt(1);
            }
            DAOUtils.closeQueries(ps, rs);

            insertarRastreoForm.setId_rastreo(id_rastreo);
            ps = c.prepareStatement("INSERT INTO cartucho_rastreo(id_cartucho, id_rastreo) VALUES (?, ?)");
            ps.setInt(1, id_cartucho);
            ps.setInt(2, id_rastreo);
            ps.executeUpdate();

            c.commit();
        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException e1) {
                Logger.putLog("Exception: ", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
                throw e;
            }
            Logger.putLog("Exception", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
        return "";
    }

    public static boolean isAutomaticCrawler(Connection c, long id_rastreo) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = c.prepareStatement("SELECT automatico FROM rastreo WHERE id_rastreo = ?");
            ps.setLong(1, id_rastreo);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("automatico") == 1;
            }
        } catch (Exception e) {
            Logger.putLog("Exception", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
        return false;
    }

    private static InsertarRastreoForm updateLists(Connection c, InsertarRastreoForm insertarRastreoForm) throws Exception {

        UpdateListDataForm updateListDataForm = new UpdateListDataForm();

        updateListDataForm.setListaRastreable(insertarRastreoForm.getListaRastreable());
        updateListDataForm.setIdListaRastreable(insertarRastreoForm.getId_lista_rastreable());
        updateListDataForm.setListaNoRastreable(insertarRastreoForm.getListaNoRastreable());
        updateListDataForm.setIdListaNoRastreable(insertarRastreoForm.getId_lista_no_rastreable());
        updateListDataForm.setNombre(insertarRastreoForm.getCodigo());

        SemillaDAO.updateLists(c, updateListDataForm);

        insertarRastreoForm.setId_lista_rastreable(updateListDataForm.getIdListaRastreable());
        insertarRastreoForm.setId_lista_no_rastreable(updateListDataForm.getIdListaNoRastreable());
        insertarRastreoForm.setIdRastreableAntiguo(updateListDataForm.getIdRastreableAntiguo());
        insertarRastreoForm.setIdNoRastreableAntiguo(updateListDataForm.getIdNoRastreableAntiguo());

        return insertarRastreoForm;
    }

    private static void removeLists(Connection c, InsertarRastreoForm insertarRastreoForm) throws Exception {

        UpdateListDataForm updateListDataForm = new UpdateListDataForm();

        updateListDataForm.setListaRastreable(insertarRastreoForm.getListaRastreable());
        updateListDataForm.setIdRastreableAntiguo(insertarRastreoForm.getIdRastreableAntiguo());
        updateListDataForm.setListaNoRastreable(insertarRastreoForm.getListaNoRastreable());
        updateListDataForm.setIdNoRastreableAntiguo(insertarRastreoForm.getIdNoRastreableAntiguo());

        SemillaDAO.removeLists(c, updateListDataForm);
    }

    public static void updateRastreo(InsertarRastreoForm insertarRastreoForm) throws Exception {

        Connection c = null;
        // Editamos los rastreos asociados
        try {
            c = DataBaseManager.getConnection();
            c.setAutoCommit(false);
            updateLists(c, insertarRastreoForm);
            //Antes aqui se llamaba al metodo abajo comentado BOH!!!
            modificarRastreo(c, false, insertarRastreoForm, insertarRastreoForm.getId_rastreo());
            removeLists(c, insertarRastreoForm);
            c.commit();
        } catch (Exception e) {
            Logger.putLog("Error: ", CuentaUsuarioDAO.class, Logger.LOG_LEVEL_ERROR, e);
            try {
                if (c != null) {
                    c.rollback();
                }
            } catch (Exception excep) {
                Logger.putLog("Error al volver al estado anterior de la base de datos", CuentaUsuarioDAO.class, Logger.LOG_LEVEL_ERROR, e);
                throw e;
            }
            throw e;
        } finally {
            DataBaseManager.closeConnection(c);
        }
    }

    public static void modificarRastreo(Connection c, boolean esMenu, InsertarRastreoForm insertarRastreoForm, Long idRastreo) throws Exception {
        PreparedStatement ps = null;
        try {
            PropertiesManager pmgr = new PropertiesManager();
            boolean changeName = true;
            if (insertarRastreoForm.getCuenta_cliente() != null && insertarRastreoForm.getCuenta_cliente() != 0 && !RastreoDAO.isAutomaticCrawler(c, idRastreo)) {
                changeName = false;
            }
            if (!esMenu) {
                if (changeName) {
                    ps = c.prepareStatement("UPDATE rastreo SET fecha = ?, profundidad = ?, topn = ?, lista_no_rastreable = ?, lista_rastreable = ?, pseudoaleatorio = ?, id_language = ?, id_guideline = ?, semillas = ?, nombre_rastreo = ?, exhaustive = ?, in_directory = ? WHERE id_rastreo = ?");
                    ps.setString(8, null);
                    ps.setString(10, insertarRastreoForm.getCodigo());
                    ps.setBoolean(11, insertarRastreoForm.isExhaustive());
                    ps.setBoolean(12, insertarRastreoForm.isInDirectory());
                    ps.setLong(13, idRastreo);
                } else {
                    ps = c.prepareStatement("UPDATE rastreo SET fecha = ?, profundidad = ?, topn = ?, lista_no_rastreable = ?, lista_rastreable = ?, pseudoaleatorio = ?, id_language = ?, id_guideline = ?, semillas = ?, exhaustive = ?, in_directory = ? WHERE id_rastreo = ?");
                    ps.setBoolean(10, insertarRastreoForm.isExhaustive());
                    ps.setBoolean(11, insertarRastreoForm.isInDirectory());
                    ps.setLong(12, idRastreo);
                }

                if (insertarRastreoForm.getCartucho().equals(pmgr.getValue(CRAWLER_PROPERTIES, "cartridge.intav.id"))) {
                    //Incluimos la norma dependiendo de el valor de los enlaces rotos
                    if (insertarRastreoForm.getNormaAnalisisEnlaces() != null &&
                            insertarRastreoForm.getNormaAnalisisEnlaces().equals("1")) {
                        if (insertarRastreoForm.getNormaAnalisis().equals(pmgr.getValue(CRAWLER_PROPERTIES, "cartridge.une.intav.id"))) {
                            ps.setString(8, pmgr.getValue(CRAWLER_PROPERTIES, "cartridge.une.intav.aux.id"));
                        } else if (insertarRastreoForm.getNormaAnalisis().equals(pmgr.getValue(CRAWLER_PROPERTIES, "cartridge.wcag1.intav.id"))) {
                            ps.setString(8, pmgr.getValue(CRAWLER_PROPERTIES, "cartridge.wcag1.intav.aux.id"));
                        } else {
                            ps.setString(8, insertarRastreoForm.getNormaAnalisis());
                        }

                    } else {
                        ps.setString(8, insertarRastreoForm.getNormaAnalisis());
                    }
                } else {
                    ps.setString(8, null);
                }

                ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                ps.setInt(2, insertarRastreoForm.getProfundidad());
                ps.setLong(3, insertarRastreoForm.getTopN());
                if (insertarRastreoForm.getId_lista_no_rastreable() != 0) {
                    ps.setLong(4, insertarRastreoForm.getId_lista_no_rastreable());
                } else {
                    ps.setString(4, null);
                }
                if (insertarRastreoForm.getId_lista_rastreable() != 0) {
                    ps.setLong(5, insertarRastreoForm.getId_lista_rastreable());
                } else {
                    ps.setString(5, null);
                }
                ps.setBoolean(6, insertarRastreoForm.isPseudoAleatorio());
                ps.setLong(7, insertarRastreoForm.getLenguaje());
                ps.setLong(9, insertarRastreoForm.getId_semilla());
                ps.setBoolean(11, insertarRastreoForm.isExhaustive());
                ps.executeUpdate();

                if (insertarRastreoForm.getCartucho() != null && !insertarRastreoForm.getCartucho().isEmpty()) {
                    ps = c.prepareStatement("UPDATE cartucho_rastreo SET id_cartucho = ? WHERE id_rastreo = ?");
                    ps.setInt(1, Integer.parseInt(insertarRastreoForm.getCartucho()));
                    ps.setLong(2, idRastreo);
                    ps.executeUpdate();
                }
            } else {
                ps = c.prepareStatement("UPDATE rastreo SET profundidad = ?, topn = ?  WHERE id_rastreo = ?");
                ps.setInt(1, insertarRastreoForm.getProfundidad());
                ps.setLong(2, insertarRastreoForm.getTopN());
                ps.setLong(3, idRastreo);
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            Logger.putLog("Exception", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, null);
        }
    }

    public static int getNumActiveCrawlings(Connection conn, int cartridge) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("SELECT COUNT(*) FROM rastreo r " +
                    "JOIN cartucho_rastreo cr ON (r.id_rastreo = cr.id_rastreo)" +
                    " WHERE estado = ? AND cr.id_cartucho = ?");
            ps.setInt(1, Constants.STATUS_LAUNCHED);
            ps.setInt(2, cartridge);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            Logger.putLog("Exception: ", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
    }

    public static Long addFulfilledCrawling(Connection conn, DatosCartuchoRastreoForm dcrForm,
                                            Long idFulfilledObservatory, Long idUser) throws Exception {
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = conn.prepareStatement("INSERT INTO rastreos_realizados (id_rastreo, fecha, id_usuario, id_cartucho, id_obs_realizado, id_lista) VALUES (?,?,?,?,?,?)");
            pst.setLong(1, dcrForm.getId_rastreo());
            pst.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            pst.setLong(3, idUser);
            pst.setLong(4, dcrForm.getId_cartucho());
            if (idFulfilledObservatory != null) {
                pst.setLong(5, idFulfilledObservatory);
            } else {
                pst.setString(5, null);
            }
            pst.setLong(6, dcrForm.getIdSemilla());
            pst.executeUpdate();
            DAOUtils.closeQueries(pst, rs);

            pst = conn.prepareStatement("SELECT id FROM rastreos_realizados WHERE id_rastreo = ? ORDER BY fecha desc LIMIT 1");
            pst.setLong(1, dcrForm.getId_rastreo());
            rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getLong("id");
            } else {
                return null;
            }
        } catch (SQLException e) {
            Logger.putLog("Error al añadir un rastreo realizado: ", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(pst, rs);
        }
    }

    public static Long addFulfilledObservatory(Connection conn, Long idObservatory, Long idCartridge) throws Exception {
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = conn.prepareStatement("INSERT INTO observatorios_realizados (id_observatorio, fecha, id_cartucho) VALUES (?,?,?)");
            pst.setLong(1, idObservatory);
            pst.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            pst.setLong(3, idCartridge);
            pst.executeUpdate();
            DAOUtils.closeQueries(pst, rs);

            pst = conn.prepareStatement("SELECT id FROM observatorios_realizados WHERE id_observatorio = ? ORDER BY fecha desc LIMIT 1");
            pst.setLong(1, idObservatory);
            rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getLong("id");
            } else {
                return null;
            }
        } catch (SQLException e) {
            Logger.putLog("Error al añadir un rastreo realizado: ", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(pst, rs);
        }
    }


    public static List<FulFilledCrawling> getFulfilledCrawlings(Connection conn, Long idCrawling, CargarRastreosRealizadosSearchForm searchForm, Long idFulfilledObservatory, int pagina) throws Exception {
        List<FulFilledCrawling> crawlings = new ArrayList<FulFilledCrawling>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        PropertiesManager pmgr = new PropertiesManager();
        int pagSize = Integer.parseInt(pmgr.getValue(CRAWLER_PROPERTIES, "pagination.size"));
        int resultFrom = pagSize * pagina;
        int count = 1;
        try {
            String query = "SELECT rr.* , u.usuario, c.aplicacion, l.nombre " +
                    "FROM rastreos_realizados rr " +
                    "JOIN usuario u ON (u.id_usuario = rr.id_usuario) " +
                    "JOIN cartucho c ON (rr.id_cartucho = c.id_cartucho) " +
                    "LEFT JOIN lista l ON (rr.id_lista = l.id_lista) " +
                    "WHERE id_rastreo = ? ";


            if (searchForm != null) {
                if (searchForm.getInitial_date() != null && !searchForm.getInitial_date().isEmpty()) {
                    query += " AND rr.fecha >= ?";
                }
                if (searchForm.getFinal_date() != null && !searchForm.getFinal_date().isEmpty()) {
                    query += " AND rr.fecha <= ? ";
                }
                if (searchForm.getCartridge() != null && !searchForm.getCartridge().isEmpty()) {
                    query += " AND rr.id_cartucho = ? ";
                }
                if (searchForm.getSeed() != null && !searchForm.getSeed().isEmpty()) {
                    query += " AND rr.id_lista = ? ";
                }
            }

            if (idFulfilledObservatory != null) {
                query += " AND rr.id_obs_realizado = ? ";
            }

            query += "ORDER BY fecha DESC LIMIT ? OFFSET ?;";
            ps = conn.prepareStatement(query);
            ps.setLong(count++, idCrawling);

            if (searchForm != null) {
                if (searchForm.getInitial_date() != null && !searchForm.getInitial_date().isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = sdf.parse(searchForm.getInitial_date());
                    ps.setTimestamp(count++, new Timestamp(date.getTime()));
                }
                if (searchForm.getFinal_date() != null && !searchForm.getFinal_date().isEmpty()) {
                    searchForm.setFinal_date(searchForm.getFinal_date());
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                    Date date = sdf.parse(searchForm.getFinal_date() + " 23:59:59");
                    ps.setTimestamp(count++, new Timestamp(date.getTime()));
                }
                if (searchForm.getCartridge() != null && !searchForm.getCartridge().isEmpty()) {
                    ps.setLong(count++, Long.parseLong(searchForm.getCartridge()));
                }
                if (searchForm.getSeed() != null && !searchForm.getSeed().isEmpty()) {
                    ps.setLong(count++, Long.parseLong(searchForm.getSeed()));
                }
            }

            if (idFulfilledObservatory != null) {
                ps.setLong(count++, idFulfilledObservatory);
            }

            ps.setInt(count++, pagSize);
            ps.setInt(count, resultFrom);
            rs = ps.executeQuery();

            while (rs.next()) {
                FulFilledCrawling fulfilledCrawling = new FulFilledCrawling();
                fulfilledCrawling.setId(rs.getLong("id"));
                fulfilledCrawling.setIdCrawling(rs.getLong("id_rastreo"));
                fulfilledCrawling.setUser(rs.getString("usuario"));
                fulfilledCrawling.setDate(rs.getTimestamp("fecha"));
                fulfilledCrawling.setIdCartridge(rs.getLong("id_cartucho"));
                fulfilledCrawling.setCartridge(rs.getString("aplicacion"));
                fulfilledCrawling.setIdFulfilledObservatory(rs.getLong("id_obs_realizado"));
                SemillaForm semillaForm = new SemillaForm();
                semillaForm.setNombre(rs.getString("nombre"));
                fulfilledCrawling.setSeed(semillaForm);
                crawlings.add(fulfilledCrawling);
            }

            return crawlings;
        } catch (SQLException e) {
            Logger.putLog("Exception: ", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
    }

    public static Map<Long, List<FulFilledCrawling>> getFulfilledCrawlings(Connection conn, List<ResultadoSemillaForm> seedsResults, Long idFulfilledObservatory) throws Exception {
        Map<Long, List<FulFilledCrawling>> results = new HashMap<Long, List<FulFilledCrawling>>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean isWhere = false;
        int count = 1;
        try {
            String query = "SELECT rr.* , u.usuario, c.aplicacion, l.nombre " +
                    "FROM rastreos_realizados rr " +
                    "JOIN usuario u ON (u.id_usuario = rr.id_usuario) " +
                    "JOIN cartucho c ON (rr.id_cartucho = c.id_cartucho) " +
                    "LEFT JOIN lista l ON (rr.id_lista = l.id_lista) ";

            if (seedsResults.size() != 0) {
                query += "WHERE id_rastreo IN ";
                StringBuilder IdStrList = new StringBuilder(" (");
                for (int i = 1; i <= seedsResults.size(); i++) {
                    if (seedsResults.size() > i) {
                        IdStrList.append("?,");
                    } else if (seedsResults.size() == i) {
                        IdStrList.append("?) ");
                    }
                }
                query += IdStrList;
                isWhere = true;
            }


            if (idFulfilledObservatory != null) {
                if (isWhere) {
                    query += " AND rr.id_obs_realizado = ? ";
                } else {
                    query += " WHERE rr.id_obs_realizado = ? ";
                }
            }

            ps = conn.prepareStatement(query);

            for (ResultadoSemillaForm seedsResult : seedsResults) {
                ps.setLong(count++, Long.parseLong(seedsResult.getIdCrawling()));
            }

            if (idFulfilledObservatory != null) {
                ps.setLong(count, idFulfilledObservatory);
            }

            rs = ps.executeQuery();

            while (rs.next()) {
                FulFilledCrawling fulfilledCrawling = new FulFilledCrawling();
                fulfilledCrawling.setId(rs.getLong("id"));
                fulfilledCrawling.setIdCrawling(rs.getLong("id_rastreo"));
                fulfilledCrawling.setUser(rs.getString("usuario"));
                fulfilledCrawling.setDate(rs.getTimestamp("fecha"));
                fulfilledCrawling.setIdCartridge(rs.getLong("id_cartucho"));
                fulfilledCrawling.setCartridge(rs.getString("aplicacion"));
                fulfilledCrawling.setIdFulfilledObservatory(rs.getLong("id_obs_realizado"));
                SemillaForm semillaForm = new SemillaForm();
                semillaForm.setNombre(rs.getString("nombre"));
                fulfilledCrawling.setSeed(semillaForm);
                if (!results.containsKey(rs.getLong("id_rastreo"))) {
                    results.put(rs.getLong("id_rastreo"), new ArrayList<FulFilledCrawling>());
                }
                results.get(rs.getLong("id_rastreo")).add(fulfilledCrawling);
            }

            return results;
        } catch (SQLException e) {
            Logger.putLog("Exception: ", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
    }

    public static List<FulFilledCrawling> getOldCrawlings(Connection conn, int numDays) throws Exception {
        List<FulFilledCrawling> crawlings = new ArrayList<FulFilledCrawling>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - numDays);

            ps = conn.prepareStatement("SELECT * FROM rastreos_realizados rr " +
                    "JOIN observatorios_realizados ob ON (rr.id_obs_realizado = ob.id) " +
                    "WHERE rr.fecha > ?");
            ps.setDate(1, new java.sql.Date(calendar.getTimeInMillis()));
            rs = ps.executeQuery();
            while (rs.next()) {
                FulFilledCrawling fulfilledCrawling = new FulFilledCrawling();
                fulfilledCrawling.setId(rs.getLong("id"));
                fulfilledCrawling.setIdCrawling(rs.getLong("id_rastreo"));
                fulfilledCrawling.setIdObservatory(rs.getLong("id_observatorio"));
                fulfilledCrawling.setIdCartridge(rs.getLong("id_cartucho"));
                fulfilledCrawling.setUser(rs.getString("id_usuario"));
                fulfilledCrawling.setDate(rs.getTimestamp("fecha"));
                crawlings.add(fulfilledCrawling);
            }

            return crawlings;
        } catch (SQLException e) {
            Logger.putLog("Exception: ", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
    }

    public static String getNombreNorma(Connection conn, long idNorma) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("SELECT * FROM tguidelines WHERE cod_guideline = ?;");
            ps.setLong(1, idNorma);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("des_guideline").substring(0, rs.getString("des_guideline").length() - 4).toUpperCase();
            }
            return null;
        } catch (SQLException e) {
            Logger.putLog("Exception: ", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
    }

    public static String getFullfilledCrawlingEntityName(Connection conn, long idExecution) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("SELECT nombre FROM cuenta_cliente c " +
                    "JOIN rastreo r ON (r.id_cuenta = c.id_cuenta) " +
                    "JOIN rastreos_realizados rr ON (r.id_rastreo = rr.id_rastreo) " +
                    "WHERE rr.id = ?;");
            ps.setLong(1, idExecution);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("nombre");
            }
            return null;
        } catch (SQLException e) {
            Logger.putLog("Exception: ", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
    }

    private static List<String> convertStringToList(String lista) {
        List<String> urlsList = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(lista, ";");
        while (tokenizer.hasMoreTokens()) {
            urlsList.add(tokenizer.nextToken());
        }
        return urlsList;
    }

    public static void enableDisableCrawler(Connection conn, long idCrawler, boolean activo) throws Exception {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("UPDATE rastreo SET activo = ? WHERE id_rastreo = ?");
            ps.setBoolean(1, activo);
            ps.setLong(2, idCrawler);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.putLog("Exception: ", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, null);
        }
    }

    public static long getSeedByCrawler(Connection c, long idCrawler) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = c.prepareStatement("SELECT semillas FROM rastreo WHERE id_rastreo = ?");
            ps.setLong(1, idCrawler);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getLong("semillas");
            }
        } catch (SQLException e) {
            Logger.putLog("Exception: ", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
        return -1;
    }

    /*public static int getNumObservatoryAnalysePages(Connection c, List<Long> ids) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            StringBuilder IdStrList = new StringBuilder("(");
            for (int i = 1; i <= ids.size(); i++) {
                if (ids.size() > i) {
                    IdStrList.append("?,");
                } else if (ids.size() == i) {
                    IdStrList.append("?)");
                }
            }

            ps = c.prepareStatement("SELECT COUNT(*) FROM tanalisis WHERE cod_rastreo IN " + IdStrList);
            for (int i = 0; i < ids.size(); i++) {
                ps.setLong(i + 1, ids.get(i));
            }
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            Logger.putLog("Exception: ", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
        return -1;
    }*/

    /*public static List<Long> getExecutedCrawlerCategoryIds(Connection connR, long id_observatorio, long id_categoria) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Long> executedCrawlerIds = new ArrayList<Long>();
        try {
            //RECUPERAMOS LOS IDS DE LOS RASTREOS REALIZADOS PARA EL OBSERVATORIO
            ps = connR.prepareStatement("SELECT rr.id FROM rastreo r " +
                    "JOIN rastreos_realizados rr ON (r.id_rastreo = rr.id_rastreo) " +
                    "JOIN observatorio o ON (o.id_observatorio = r.id_observatorio )" +
                    "WHERE r.id_observatorio = ? AND o.id_categoria = ?");
            ps.setLong(1, id_observatorio);
            ps.setLong(2, id_categoria);
            rs = ps.executeQuery();
            while (rs.next()) {
                executedCrawlerIds.add(rs.getLong("id"));
            }
            return executedCrawlerIds;
        } catch (Exception e) {
            Logger.putLog("Exception: ", ObservatorioDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
    }*/

    public static void updateCrawlerName(Connection c, String name, long crawlerId) throws Exception {
        PreparedStatement ps = null;
        try {
            ps = c.prepareStatement("UPDATE rastreo SET nombre_rastreo = ? WHERE id_rastreo = ?");
            ps.setString(1, name);
            ps.setLong(2, crawlerId);
            ps.executeUpdate();
        } catch (Exception e) {
            Logger.putLog("Exception: ", ObservatorioDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, null);
        }
    }

    public static Long getCrawlerFromSeedAndObservatory(Connection c, long id_seed, long id_observatory) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = c.prepareStatement("SELECT id_rastreo FROM rastreo r WHERE r.id_observatorio = ? AND r.semillas = ?");
            ps.setLong(1, id_observatory);
            ps.setLong(2, id_seed);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (Exception e) {
            Logger.putLog("Exception: ", ObservatorioDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }

        return null;
    }

    public static List<Long> getCrawlerCategoryIds(Connection connR, long id_observatorio, long id_categoria) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Long> crawlerIds = new ArrayList<Long>();
        try {
            //RECUPERAMOS LOS IDS DE LOS RASTREOS PARA EL OBSERVATORIO
            ps = connR.prepareStatement("SELECT id_rastreo FROM rastreo r " +
                    "JOIN observatorio o ON (r.id_observatorio = o.id_observatorio) " +
                    "JOIN observatorio_categoria oc ON (oc.id_observatorio = o.id_observatorio)" +
                    "WHERE r.id_observatorio = ? AND oc.id_categoria = ? AND r.semillas IN (" +
                    "SELECT id_lista FROM lista WHERE id_categoria = ?)");
            ps.setLong(1, id_observatorio);
            ps.setLong(2, id_categoria);
            ps.setLong(3, id_categoria);
            rs = ps.executeQuery();
            while (rs.next()) {
                crawlerIds.add(rs.getLong("id_rastreo"));
            }
            return crawlerIds;
        } catch (Exception e) {
            Logger.putLog("Exception: ", ObservatorioDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
    }

    public static PeriodicidadForm getRecurrence(Connection conn, long idRecurrence) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("SELECT * FROM periodicidad WHERE id_periodicidad = ?");
            ps.setLong(1, idRecurrence);
            rs = ps.executeQuery();
            if (rs.next()) {
                PeriodicidadForm periodicidadForm = new PeriodicidadForm();
                periodicidadForm.setId(rs.getLong("id_periodicidad"));
                periodicidadForm.setNombre(rs.getString("nombre"));
                periodicidadForm.setCronExpression(rs.getString("cronExpression"));
                periodicidadForm.setDias(rs.getInt("dias"));

                return periodicidadForm;
            }
        } catch (Exception e) {
            Logger.putLog("Exception: ", ObservatorioDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }

        return null;
    }

    public static FulfilledCrawlingForm getFullfilledCrawlingExecution(Connection conn, long idExecution) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("SELECT rr.*, l.*, cl.* FROM rastreos_realizados rr " +
                    "JOIN lista l ON (rr.id_lista = l.id_lista) " +
                    "LEFT JOIN categorias_lista cl ON (l.id_categoria = cl.id_categoria) " +
                    "WHERE id = ? ");
            ps.setLong(1, idExecution);
            rs = ps.executeQuery();
            if (rs.next()) {
                FulfilledCrawlingForm form = new FulfilledCrawlingForm();
                form.setId(rs.getString("rr.id"));
                form.setDate(CrawlerUtils.formatDate(rs.getDate("fecha")));
                form.setIdCrawling(String.valueOf(rs.getLong("id_rastreo")));
                form.setIdCartridge(String.valueOf(rs.getLong("id_cartucho")));

                SemillaForm semilla = new SemillaForm();
                semilla.setId(rs.getLong("l.id_lista"));
                semilla.setAcronimo(rs.getString("acronimo"));
                semilla.setNombre(rs.getString("l.nombre"));
                semilla.setDependencia(rs.getString("dependencia"));
                semilla.setListaUrlsString(rs.getString("l.lista"));

                CategoriaForm categoria = new CategoriaForm();
                categoria.setName(rs.getString("cl.nombre"));
                categoria.setId(rs.getString("cl.id_categoria"));
                semilla.setCategoria(categoria);

                form.setSeed(semilla);
                return form;
            }
            return null;
        } catch (SQLException e) {
            Logger.putLog("Exception: ", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
    }

    public static void setObservatoryExecutionToCrawlerExecution(Connection conn, long idObsExecution, long idExecuteCrawler) throws Exception {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("UPDATE rastreos_realizados SET id_obs_realizado = ? WHERE id = ?");
            ps.setLong(1, idObsExecution);
            ps.setLong(2, idExecuteCrawler);
            ps.executeUpdate();

        } catch (SQLException e) {
            Logger.putLog("Exception: ", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, null);
        }
    }

    public static FulfilledCrawlingForm getExecutedCrawling(Connection c, Long idCrawling, Long idSeed) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = c.prepareStatement("SELECT * FROM rastreos_realizados rr WHERE id_rastreo = ? AND id_lista = ? ORDER BY id DESC");
            ps.setLong(1, idCrawling);
            ps.setLong(2, idSeed);
            rs = ps.executeQuery();
            if (rs.next()) {
                FulfilledCrawlingForm form = new FulfilledCrawlingForm();
                form.setId(rs.getString("id"));
                form.setDate(CrawlerUtils.formatDate(rs.getDate("fecha")));
                form.setIdCrawling(String.valueOf(rs.getLong("id_rastreo")));
                form.setIdCartridge(String.valueOf(rs.getLong("id_cartucho")));
                return form;
            }
            return null;
        } catch (SQLException e) {
            Logger.putLog("Exception: ", RastreoDAO.class, Logger.LOG_LEVEL_ERROR, e);
            throw e;
        } finally {
            DAOUtils.closeQueries(ps, rs);
        }
    }
}