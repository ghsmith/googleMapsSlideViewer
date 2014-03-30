package dp.slideviewer.servlet;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import dp.slideviewer.ndpread.ImageInformation;
import dp.slideviewer.ndpread.JpegImageCreator;
import dp.slideviewer.ndpread.NdpReadWrapper;
import dp.slideviewer.ndpread.TilePositionCalculator;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openslide.OpenSlide;

/**
 *
 * @author one
 */
public class GetTile extends HttpServlet {

    static ConcurrentHashMap<String, OpenSlide> svsMap = new ConcurrentHashMap<String, OpenSlide>();
    
    protected class SessionNdpReadSupport {

        protected String dummy = "dummy";
        protected NdpReadWrapper wrapper = new NdpReadWrapper();
        protected JpegImageCreator imageCreatorMed = new JpegImageCreator(0.5f);
        protected JpegImageCreator imageCreatorHigh = new JpegImageCreator(0.8f);
        protected Map<String, ImageInformation> iiMap = new ConcurrentHashMap();
        protected Map<String, TilePositionCalculator> tpcMap = new ConcurrentHashMap();
        
    }

    protected class SessionSvsReadSupport {

        protected String dummy = "dummy";
        //protected String fileName = null;
        //protected OpenSlide openSlide = null;
        protected BufferedImage bi = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        protected Graphics2D biContext = bi.createGraphics();
        
    }
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String fileName = request.getParameter("fileName");

        if(fileName.endsWith(".ndpi")) {
        
            SessionNdpReadSupport snrs = null;

            synchronized(this) {

                snrs = (SessionNdpReadSupport)request.getSession().getAttribute("snrs");
                if(snrs == null) {
                    snrs = new SessionNdpReadSupport();
                    request.getSession().setAttribute("snrs", snrs);
                }

            }

            synchronized(snrs.dummy) {

                NdpReadWrapper wrapper = snrs.wrapper;
                JpegImageCreator imageCreatorMed = snrs.imageCreatorMed;
                JpegImageCreator imageCreatorHigh = snrs.imageCreatorHigh;
                Map<String, ImageInformation> iiMap = snrs.iiMap;
                Map<String, TilePositionCalculator> tpcMap = snrs.tpcMap;

                response.setContentType("image/jpg");

                OutputStream os = null;

                try {

                    int x = Integer.valueOf(request.getParameter("x")).intValue();
                    int y = Integer.valueOf(request.getParameter("y")).intValue();
                    int z = Integer.valueOf(request.getParameter("z")).intValue();

                    float mag = -1;
                    switch (z) {
                        case 0:
                            mag = 0.078125f;
                            break;
                        case 1:
                            mag = 0.15625f;
                            break;
                        case 2:
                            mag = 0.3125f;
                            break;
                        case 3:
                            mag = 0.625f;
                            break;
                        case 4:
                            mag = 1.25f;
                            break;
                        case 5:
                            mag = 2.5f;
                            break;
                        case 6:
                            mag = 5f;
                            break;
                        case 7:
                            mag = 10f;
                            break;
                        case 8:
                            mag = 20f;
                            break;
                        case 9:
                            mag = 40f;
                            break;
                    }

                    ImageInformation ii = iiMap.get(fileName);
                    if (ii == null) {
                        ii = wrapper.getImageInformation(fileName);
                        iiMap.put(fileName, ii);
                    }

                    TilePositionCalculator tpc = tpcMap.get(fileName + "." + z);
                    if (tpc == null) {
                        tpc = new TilePositionCalculator(ii, 256, 256, mag);
                        tpcMap.put(fileName, tpc);
                    }

                    if (x >= 0 && x < tpc.getTileXPositions().size()
                            && y >= 0 && y < tpc.getTileYPositions().size()) {

                        byte[] bs = wrapper.getImageSegment(
                                fileName,
                                tpc.getTileXPositions().get(x).intValue(),
                                tpc.getTileYPositions().get(y).intValue(),
                                0,
                                mag,
                                256,
                                256);

                        os = new ByteArrayOutputStream(8192);

                        if(z == 0)
                            imageCreatorHigh.createImageFromNdpiBytes(bs, 256, 256, os);
                        else
                            imageCreatorMed.createImageFromNdpiBytes(bs, 256, 256, os);

                        ((ByteArrayOutputStream)os).writeTo(response.getOutputStream());

                    }

                } catch (Exception e) {

                    throw new ServletException(e);

                } finally {

                    if(os != null)
                        os.close();

                }

            }
        
        }
        
        else if(fileName.endsWith(".svs")) {

            SessionSvsReadSupport ssvs = null;

            synchronized(this) {

                ssvs = (SessionSvsReadSupport)request.getSession().getAttribute("ssvs");
                if(ssvs == null) {
                    ssvs = new SessionSvsReadSupport();
                    request.getSession().setAttribute("ssvs", ssvs);
                }

            }

            synchronized(ssvs.dummy) {
            
                //if(
                //    (ssvs.fileName == null || !ssvs.fileName.equals(fileName))
                //) {
                //    if(ssvs.openSlide != null) {
                //        try {
                //            ssvs.openSlide.close();
                //        }
                //        catch(Exception e) {
                //        }
                //    }
                //    ssvs.fileName = fileName;
                //    ssvs.openSlide = new OpenSlide(new File(fileName));
                //}
                //
                //OpenSlide openSlide = ssvs.openSlide;
                //BufferedImage bi = ssvs.bi;
                //Graphics2D biContext = ssvs.biContext;

                OpenSlide openSlide = svsMap.get(fileName);
                if(openSlide == null) {
                    openSlide = new OpenSlide(new File(fileName));
                    svsMap.put(fileName, openSlide);
                }
                BufferedImage bi = ssvs.bi;
                Graphics2D biContext = ssvs.biContext;

                response.setContentType("image/jpg");
                
                OutputStream os = null;

                try {

                    int x = Integer.valueOf(request.getParameter("x")).intValue();
                    int y = Integer.valueOf(request.getParameter("y")).intValue();
                    int z = Integer.valueOf(request.getParameter("z")).intValue();

                    float mag = -1;
                    switch (z) {
                        case 0:
                            mag = 0.078125f;
                            break;
                        case 1:
                            mag = 0.15625f;
                            break;
                        case 2:
                            mag = 0.3125f;
                            break;
                        case 3:
                            mag = 0.625f;
                            break;
                        case 4:
                            mag = 1.25f;
                            break;
                        case 5:
                            mag = 2.5f;
                            break;
                        case 6:
                            mag = 5f;
                            break;
                        case 7:
                            mag = 10f;
                            break;
                        case 8:
                            mag = 20f;
                            break;
                        //case 9:
                        //    mag = 40f;
                        //    break;
                    }

                    double downSample = 20f / mag;

                    biContext.clearRect(0, 0, 256, 256);
                    openSlide.paintRegion(biContext, 0, 0, (x * 256), (y * 256), 256, 256, downSample);
                    os = new ByteArrayOutputStream(16384);
                    JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os); 
                    JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
                    param.setQuality(z == 0 ? 0.8f : 0.5f, true);
                    encoder.encode(bi, param); 
                    ((ByteArrayOutputStream)os).writeTo(response.getOutputStream());

                } catch (Exception e) {

                    throw new ServletException(e);

                } finally {

                    if(os != null) {
                        os.close();
                    }
                    
                }
                
            }
            
        }

    }
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
        return;
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
        return;
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
