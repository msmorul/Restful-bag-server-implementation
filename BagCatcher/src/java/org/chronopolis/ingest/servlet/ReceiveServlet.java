/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.ServerException;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author toaster
 */
public class ReceiveServlet extends HttpServlet {

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        File dropDir = (File) getServletContext().getAttribute(DropBoxConfigurationContext.PARAM_BOX);

        
        String pathinfo = req.getPathInfo().substring(1);
        if (pathinfo.contains("/")) {
            throw new ServerException("Bad Characters in path " + pathinfo);
        }

        ServletInputStream sis = req.getInputStream();
        File outfile = new File(dropDir, pathinfo);
        int rev = 1;
        while (outfile.exists())
        {
            outfile = new File(dropDir,pathinfo + "_" + rev);
            rev++;
        }
        System.out.println("opening file " + outfile);
        OutputStream fos = new FileOutputStream(outfile);
        byte[] block = new byte[32768];
        int read = 0;

        while ((read = sis.read(block)) != -1) {
            fos.write(block, 0, read);
        }
        fos.close();
        System.out.println("closing file");

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
