/*
 * Copyright 2018 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.pass.deposit.messaging.config.repository;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BasicAuthRealm extends AuthRealm {

    private static final String MECH = "basic";

    @JsonProperty("url")
    private URL baseUrl;

    private String username;

    private String password;

    @JsonProperty("realm-name")
    private String realmName;

    public BasicAuthRealm() {
        this.setMech(MECH);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Implementation note: typed as a String so Jackson and the SpringEnvironmentDeserializer can perform property
     * interpolation.
     *
     * @return the base url of the authentication realm
     */
    public String getBaseUrl() {
        return baseUrl.toString();
    }

    /**
     * Implementation note: typed as a String so Jackson and the SpringEnvironmentDeserializer can perform property
     * interpolation.
     *
     * @param baseUrl the base URL that this {@code AuthRealm} provides authentication credentials for
     */
    public void setBaseUrl(String baseUrl) {
        try {
            this.baseUrl = new URL(baseUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Supplied URL is not a valid: " + e.getMessage(), e);
        }
    }

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        BasicAuthRealm that = (BasicAuthRealm) o;
        return Objects.equals(baseUrl, that.baseUrl) &&
               Objects.equals(username, that.username) &&
               Objects.equals(password, that.password) &&
               Objects.equals(realmName, that.realmName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), baseUrl, username, password, realmName);
    }

    @Override
    public String toString() {
        return "BasicAuthRealm{" + "baseUrl=" + baseUrl + ", username='" + username + '\'' +
               ", password='" + ((password != null) ? "xxxxx" : "<null>") + '\'' +
               ", realmName='" + realmName + '\'' + "} " + super.toString();
    }
}
