/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.json;

import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author toaster
 */
public final class PeerAuthenticator extends Authenticator
{

//    private static final Logger LOG = Logger.getLogger(PeerAuthenticator.class);

    private List<PartnerSite> sites = new ArrayList<PartnerSite>();
    
public void addSite(PartnerSite site)
{
    sites.add(site);
}
    @Override
    protected PasswordAuthentication getPasswordAuthentication()
    {
//        EntityManager em = PersistUtil.getEntityManager();

//        Query q = em.createNamedQuery("PartnerSite.listAll");

        for ( PartnerSite ps : sites )
        {
            //PartnerSite ps = (PartnerSite) o;
            try
            {
                URL site = new URL(ps.getRemoteURL());


                if ( site.getHost().equals(getRequestingHost()) )
                {
                    return new java.net.PasswordAuthentication(ps.getUser(),
                            ps.getPass().toCharArray());
                }
            }
            catch ( MalformedURLException e )
            {
//                LOG.error("Cannot parse stored url, should not happen!", e);
                throw new RuntimeException(e);
            }

        }

//        em.close();
        return super.getPasswordAuthentication();
    }
}
