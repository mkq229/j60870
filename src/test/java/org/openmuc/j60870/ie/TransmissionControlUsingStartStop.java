/*
 * Copyright 2014-2023 Fraunhofer ISE
 *
 * This file is part of j60870.
 * For more information visit http://www.openmuc.org
 *
 * j60870 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * j60870 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with j60870.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.j60870.ie;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmuc.j60870.*;

import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;
import static org.openmuc.j60870.ClientServerITest.getAvailablePort;

public class TransmissionControlUsingStartStop {

    Server serverSap;
    Connection clientConnection;
    Connection serverConnection;
    ClientConnectionListenerImpl clientConnectionListener;
    ServerConnectionListenerImpl serverConnectionListener;
    IOException clientStoppedCause;
    IOException serverStoppedCause;
    boolean newAsduCalled;
    public CountDownLatch connectionWaitLatch;

    private class ClientConnectionListenerImpl implements ConnectionEventListener {

        @Override
        public void newASdu(ASdu aSdu) {
            newAsduCalled = true;
        }

        @Override
        public void connectionClosed(IOException cause) {
            clientStoppedCause = cause;
        }

        @Override
        public void dataTransferStateChanged(boolean stopped) {

        }
    }

    private class ServerConnectionListenerImpl implements ConnectionEventListener {

        @Override
        public void newASdu(ASdu aSdu) {
            newAsduCalled = true;
        }

        @Override
        public void connectionClosed(IOException cause) {
            serverStoppedCause = cause;
        }

        @Override
        public void dataTransferStateChanged(boolean stopped) {

        }
    }

    private class ServerListenerImpl implements ServerEventListener {

        @Override
        public ConnectionEventListener setConnectionEventListenerBeforeStart() {
            return serverConnectionListener;
        }

        @Override
        public void connectionIndication(Connection connection) {
            serverConnection = connection;
            connectionWaitLatch.countDown();
        }

        @Override
        public void serverStoppedListeningIndication(IOException e) {

        }

        @Override
        public void connectionAttemptFailed(IOException e) {

        }
    }

    @Before
    public void initConnection() throws IOException {
        int port = getAvailablePort();
        newAsduCalled = false;
        clientConnectionListener = new ClientConnectionListenerImpl();
        serverConnectionListener = new ServerConnectionListenerImpl();
        ServerListenerImpl serverListener = new ServerListenerImpl();
        connectionWaitLatch = new CountDownLatch(1);
        serverSap = Server.builder().setPort(port).build();
        serverSap.start(serverListener);
        clientConnection = new ClientConnectionBuilder("127.0.0.1").setPort(port).build();
        clientConnection.setConnectionListener(clientConnectionListener);
    }

    @After
    public void exitConnection() {
        clientConnection.close();
        serverSap.stop();
    }

    /***
     * 5.3.2.70 Description block 2. Expect Active Close on receipt of I- or S-frames.
     */
    @Test
    public void receiveIorSFramesinStoppedConnectionState() throws InterruptedException, IOException {
        connectionWaitLatch.await();
        clientConnection.interrogation(1, CauseOfTransmission.ACTIVATION,
                new IeQualifierOfInterrogation(20));
        Thread.sleep(1000);
        assertTrue(serverConnection.isClosed());
        assertTrue(clientConnection.isClosed());
        assertFalse(newAsduCalled);
        assertEquals(serverStoppedCause.getClass(), IOException.class);
        assertTrue(serverStoppedCause.getMessage().contains("message while STOPDT state"));
        // controlled station (server) closes because it receives an asdu while in stopped state, thus controller
        // throws EOFException because remote closed
        Thread.sleep(1000);
        assertEquals(EOFException.class, clientStoppedCause.getClass());
        assertTrue(clientStoppedCause.getMessage().contains("Connection was closed by remote."));
    }

    @Test
    public void receiveIorSFramesinStoppedConnectionStateAfterStartAndStop() throws InterruptedException, IOException {
        connectionWaitLatch.await();
        clientConnection.startDataTransfer(clientConnectionListener);
        clientConnection.stopDataTransfer();
        clientConnection.interrogation(1, CauseOfTransmission.ACTIVATION,
                new IeQualifierOfInterrogation(20));
        Thread.sleep(1000);
        assertTrue(serverConnection.isClosed());
        assertTrue(clientConnection.isClosed());
        assertFalse(newAsduCalled);
        assertEquals(serverStoppedCause.getClass(), IOException.class);
        assertTrue(serverStoppedCause.getMessage().contains("message while STOPDT state"));
        // controlled station (server) closes because it receives an asdu while in stopped state, thus controller
        // throws EOFException because remote closed
        assertEquals(EOFException.class, clientStoppedCause.getClass());
        assertTrue(clientStoppedCause.getMessage().contains("Connection was closed by remote."));
    }
}
