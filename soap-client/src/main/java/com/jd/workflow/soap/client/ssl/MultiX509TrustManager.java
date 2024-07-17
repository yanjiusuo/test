/**
 * Copyright (c) 2012-2013 Reficio (TM) - Reestablish your software!. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.workflow.soap.client.ssl;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Tom Bujok (tom.bujok@gmail.com)
 * <p>
 * Reficio™ - Reestablish your software!
 * www.reficio.org
 */
public class MultiX509TrustManager implements X509TrustManager {

    private final List<X509TrustManager> managers;

    public MultiX509TrustManager(List<X509TrustManager> managers) {
        this.managers = new ArrayList<X509TrustManager>(managers);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
        List<CertificateException> exceptions = new ArrayList<CertificateException>();
        try {
            for (X509TrustManager manager : managers) {
                manager.checkClientTrusted(x509Certificates, authType);
            }
        } catch (CertificateException ex) {
            exceptions.add(ex);
        }
        if (exceptions.size() >= managers.size()) {
            throw exceptions.iterator().next();
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
        List<CertificateException> exceptions = new ArrayList<CertificateException>();
        try {
            for (X509TrustManager manager : managers) {
                manager.checkServerTrusted(x509Certificates, authType);
            }
        } catch (CertificateException ex) {
            exceptions.add(ex);
        }
        if (exceptions.size() >= managers.size()) {
            throw exceptions.iterator().next();
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        List<X509Certificate> certs = new ArrayList<X509Certificate>();
        for (X509TrustManager manager : managers) {
            for (X509Certificate cert : manager.getAcceptedIssuers()) {
                certs.add(cert);
            }
        }
        return certs.toArray(new X509Certificate[]{});
    }
}