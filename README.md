SensorSystem
============

The SensorSystem library provides a basic framework for Apps that rely heavily on collecting and processing sensor data.

The root of every SensorSystem is a Container, which is responsible for lifecycle- and dependency-management of the Components it contains.
Examples for Components are 

* Sensors
* the AccuracyManager used to regulate power consumption
* the various timing and threading Managers
* the EventManager, which provides an event queue for the system
* various logging handlers

This library is split up into 3 parts:
<dl>
<dt style="font-weight: bold">core</dt>
<dd>the core library, that only has pure Java dependencies, but lacks some implementations</dd>

<dt style="font-weight: bold">android.core</dt>
<dd>the android library, that builds on the core library and adds android-specific implementations</dd>

<dt style="font-weight: bold">android.app</dt>
<dd>an android app for debugging, that displays the currently active components, the current values of sensors and a log of events from the event queue</dd>
</dl>
