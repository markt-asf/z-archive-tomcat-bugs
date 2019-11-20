/*
 * With thanks to:
 * - http://beej.us/guide/bgnet/html
 * - StackOverflow
 *
 * All ugliness is my responsibility.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <poll.h>
#include <fcntl.h>
#include <errno.h>
#include <time.h>

#define PORT "8080"   // Port we're listening on

// Get sockaddr, IPv4 or IPv6:
void *get_in_addr(struct sockaddr *sa)
{
    if (sa->sa_family == AF_INET) {
        return &(((struct sockaddr_in*)sa)->sin_addr);
    }

    return &(((struct sockaddr_in6*)sa)->sin6_addr);
}

// Return a listening socket
int get_listener_socket(void)
{
    int listener;     // Listening socket descriptor
    int yes=1;        // For setsockopt() SO_REUSEADDR, below
    int rv;

    struct addrinfo hints, *ai, *p;

    // Get us a socket and bind it
    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags = AI_PASSIVE;
    if ((rv = getaddrinfo(NULL, PORT, &hints, &ai)) != 0) {
        fprintf(stderr, "selectserver: %s\n", gai_strerror(rv));
        exit(1);
    }

    for(p = ai; p != NULL; p = p->ai_next) {
        listener = socket(p->ai_family, p->ai_socktype, p->ai_protocol);
        if (listener < 0) {
            continue;
        }

        // Lose the pesky "address already in use" error message
        setsockopt(listener, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int));

        if (bind(listener, p->ai_addr, p->ai_addrlen) < 0) {
            close(listener);
            continue;
        }

        break;
    }

    // If we got here, it means we didn't get bound
    if (p == NULL) {
        return -1;
    }

    freeaddrinfo(ai); // All done with this

    // Listen
    if (listen(listener, 10) == -1) {
        return -1;
    }

    return listener;
}

// Main
int main(int argc, char *argv[])
{
    int listener;
    int clientSocketfd;
    int clientSocketFlags;

    struct sockaddr_storage remoteaddr; // Client address
    socklen_t addrlen;
    char remoteIP[INET6_ADDRSTRLEN];

    int writeBufferSize = 8 * 1024;
    char writeBuffer[writeBufferSize];

    int totalBytesWritten = 0;
    int bytesWritten = 1;

    int lastError;

    int startTime;
    int endTime;

    int socketSendBufferSize;
    socklen_t optlen;

    struct pollfd *pfds = malloc(sizeof *pfds);

    if (argc < 2) {
    	socketSendBufferSize = -1;
    } else {
    	socketSendBufferSize = atoi(argv[1]);
    }

    // Fill write buffer
    memset(writeBuffer, 'A', writeBufferSize);

    // Set up and get a listening socket
    listener = get_listener_socket();

    if (listener == -1) {
        fprintf(stderr, "error getting listening socket\n");
        exit(1);
    }

    // Add the listener to poller
    pfds[0].fd = listener;
    pfds[0].events = POLLIN; // Report ready to read on incoming connection

    // Wait for a client to connect
    poll(pfds, 1, -1);

    // Assume incoming connection
    addrlen = sizeof remoteaddr;
    clientSocketfd = accept(listener, (struct sockaddr *)&remoteaddr, &addrlen);

    // Configure for non-blocking write
    clientSocketFlags = fcntl(clientSocketfd, F_GETFL, 0);
    if (clientSocketFlags == -1) {
        fprintf(stderr, "error reading client socket flags\n");
        exit(1);
    }
    clientSocketFlags |= O_NONBLOCK;
    if (fcntl(clientSocketfd, F_SETFL, clientSocketFlags) == -1) {
        fprintf(stderr, "error setting client socket flags\n");
        exit(1);
    }

    // Configure output buffer size
    if (socketSendBufferSize > 0) {
        fprintf(stderr, "Setting client socket send buffer size to [%d]\n", socketSendBufferSize);
        setsockopt(clientSocketfd, SOL_SOCKET, SO_SNDBUF, &socketSendBufferSize, sizeof(socketSendBufferSize));
    }

    // Check the send buffer size
    optlen = sizeof(socketSendBufferSize);
    getsockopt(clientSocketfd, SOL_SOCKET, SO_SNDBUF, &socketSendBufferSize, &optlen);
    fprintf(stderr, "Client socket configured with send buffer size [%d]\n", socketSendBufferSize);

    startTime = clock();

    pfds[0].fd = clientSocketfd;
    pfds[0].events = POLLOUT; // Report ready to read on incoming connection

    // Write loop
    do {
        poll(pfds, 1, -1);

        do {
			// Assume client is ready for write
			bytesWritten = send(clientSocketfd, writeBuffer, writeBufferSize, 0);

			if (bytesWritten < 0) {
				if (errno != EWOULDBLOCK) {
					fprintf(stderr, "error writing to client socket [%d] [%d]\n", errno, totalBytesWritten);
					exit(1);
				}
			} else {
				totalBytesWritten += bytesWritten;
			}
        } while (bytesWritten > 0);
    } while (totalBytesWritten < 10 * 1024 * 1024);

    endTime = clock();

	fprintf(stderr, "wrote [%d] bytes to client socket in [%f] milliseconds\n", totalBytesWritten,((double) 1000 * (endTime - startTime) / CLOCKS_PER_SEC));

    return 0;
}
