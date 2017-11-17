/*
 * Copyright 2017 Johns Hopkins University
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
package org.dataconservancy.nihms.transport.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.dataconservancy.nihms.transport.Transport;
import org.dataconservancy.nihms.transport.TransportSession;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.dataconservancy.nihms.transport.Transport.TRANSPORT_AUTHMODE;
import static org.dataconservancy.nihms.transport.Transport.TRANSPORT_PASSWORD;
import static org.dataconservancy.nihms.transport.Transport.TRANSPORT_PROTOCOL;
import static org.dataconservancy.nihms.transport.Transport.TRANSPORT_SERVER_FQDN;
import static org.dataconservancy.nihms.transport.Transport.TRANSPORT_SERVER_PORT;
import static org.dataconservancy.nihms.transport.Transport.TRANSPORT_USERNAME;
import static org.dataconservancy.nihms.transport.ftp.FtpTransportHints.BASE_DIRECTORY;
import static org.dataconservancy.nihms.transport.ftp.FtpTransportHints.DATA_TYPE;
import static org.dataconservancy.nihms.transport.ftp.FtpTransportHints.TRANSFER_MODE;
import static org.dataconservancy.nihms.transport.ftp.FtpTransportHints.USE_PASV;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class FtpTransportTest {

    private FtpClientFactory ftpClientFactory;

    private FtpTransport transport;

    private FTPClient ftpClient;

    /**
     * Hints are simple key-value pairs used to configure, or influence the behavior of, the underlying FTPClient.
     */
    private Map<String, String> expectedHints = new HashMap<String, String>() {
        {
            put(TRANSPORT_PROTOCOL, Transport.PROTOCOL.ftp.name());
            put(TRANSPORT_AUTHMODE, Transport.AUTHMODE.userpass.name());
            put(TRANSPORT_USERNAME, "nihmsftpuser");
            put(TRANSPORT_PASSWORD, "nihmsftppass");
            put(TRANSPORT_SERVER_FQDN, "example.ftp.submission.nih.org");
            put(TRANSPORT_SERVER_PORT, "21");
            // For simplicity of testing, stay in the base directory (no need to create directories upon open)
            put(BASE_DIRECTORY, "/");
            put(TRANSFER_MODE, FtpTransportHints.MODE.stream.name());
            put(USE_PASV, Boolean.TRUE.toString());
            put(DATA_TYPE, FtpTransportHints.TYPE.binary.name());
        }
    };

    /**
     * Set up a mock FtpClientFactory to supply a mock FTPClient to the FtpTransport class under test.
     */
    @Before
    public void setUp() {
        ftpClientFactory = mock(FtpClientFactory.class);
        ftpClient = mock(FTPClient.class);
        when(ftpClientFactory.newInstance(anyMap())).thenReturn(ftpClient);

        transport = new FtpTransport(ftpClientFactory);
    }

    /**
     * Open a session successfully.  User login succeeds, the file transfer mode is set, and the working directory
     * is changed to.
     *
     * @throws IOException
     */
    @Test
    public void testOpenSuccess() throws IOException {
        when(ftpClient.login(anyString(), anyString())).thenReturn(true);
        when(ftpClient.getReplyCode()).thenReturn(FTPReply.USER_LOGGED_IN);
        when(ftpClient.setFileTransferMode(anyInt())).thenReturn(true);
        when(ftpClient.getReplyCode()).thenReturn(FTPReply.COMMAND_OK);
        when(ftpClient.changeWorkingDirectory("/")).thenReturn(true);
        when(ftpClient.getReplyCode()).thenReturn(FTPReply.COMMAND_OK);

        TransportSession session = transport.open(expectedHints);

        assertNotNull(session);
        verify(ftpClient).login("nihmsftpuser", "nihmsftppass");
        verify(ftpClient).setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
        verify(ftpClient).changeWorkingDirectory("/");
    }

    /**
     * A runtime exception is thrown when the login fails.
     *
     * @throws IOException
     */
    @Test(expected = RuntimeException.class)
    public void testLoginFailure() throws IOException {
        when(ftpClient.login(anyString(), anyString())).thenReturn(false);
        when(ftpClient.getReplyString()).thenReturn("Invalid username or password");
        when(ftpClient.getReplyCode()).thenReturn(FTPReply.REQUEST_DENIED);

        transport.open(expectedHints);
    }
}