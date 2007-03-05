/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package net.openid.consumer;

import net.openid.util.InternetDateFormat;

import java.util.Date;
import java.text.ParseException;

import org.apache.log4j.Logger;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public abstract class AbstractNonceVerifier implements NonceVerifier
{
    private static Logger _log = Logger.getLogger(AbstractNonceVerifier.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    protected static InternetDateFormat _dateFormat = new InternetDateFormat();

    protected int _maxAge;

    /**
     * @param maxAge maximum token age in seconds
     */
    protected AbstractNonceVerifier(int maxAge)
    {
        _maxAge = maxAge;
    }

    public int getMaxAge()
    {
        return _maxAge / 1000;
    }

    /**
     * Checks if nonce date is valid and if it is in the max age boudary. Other checks are delegated to {@link #seen(java.util.Date, String, String)}
     */
    public int seen(String idpUrl, String nonce)
    {
        if (DEBUG) _log.debug("Verifying nonce: " + nonce);

        Date now = new Date();

        try
        {
            Date nonceDate = _dateFormat.parse(nonce);

            if (isTooOld(now, nonceDate))
            {
                _log.warn("Nonce is too old: " + nonce);
                return TOO_OLD;
            }

            return seen(now, idpUrl, nonce);
        }
        catch (ParseException e)
        {
            _log.error("Error verifying the nonce: " + nonce, e);
            return INVALID_TIMESTAMP;
        }
    }

    /**
     * Subclasses should implement this method and check if the nonce was seen before.
     * The nonce timestamp was verified at this point, it is valid and it is in the max age boudary.
     *
     * @param now The timestamp used to check the max age boudary.
     */
    protected abstract int seen(Date now, String idpUrl, String nonce);

    protected boolean isTooOld(Date now, Date nonce)
    {
        long age = now.getTime() - nonce.getTime();

        return age > _maxAge * 1000;
    }
}
