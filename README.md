JStratum
========
Reusable, Java-based implementation of the Stratum transport protocol (not specific to mining).

Why?
====
- We're working on a mining pool and weren't happy with the hackish way that most mining pool code deals with Stratum messaging.
- We're working on a wallet client that may utilize the Electrum protocol, which is based on Stratum, and therefore needed Stratum there too.
- There weren't many good Java implementations of Stratum, and the ones we did find were specific to mining (https://github.com/matt1/stratum-client-java, for example).

How is this implementation different?
=====================================
This implementation makes absolutely no assumptions about what Stratum is being used for. It can be used for mining, Electrum, or even new systems that just choose to use Stratum as their message passing system of choice.

In addition, the implementation doesn't even assume that a direct TCP connection is being used (although a TCP implementation is provided). You can choose to implement your own version of `MessageTransport` or extend `StatefulMessageTransport` to support other versions of Stratum, like HTTP or UDP. Messaging is completely decoupled from the underlying transport.

How do I use this?
==================
A mining pool sample is pending. For now, the easiest way to get started is create a class to extend `StratumTcpServer` or `StratumTcpClient`, depending upon which side of the connection you need to implement, and then just fill-in the missing methods.

How do I support this project?
==============================
Donate to us at: 1RedBPHctwHUYz1mwtyTL3xUKNB1rj28V

The more we have to work with, the faster we can develop. Our primary source of revenue tends to be client work, which takes us away from cool things like this.
