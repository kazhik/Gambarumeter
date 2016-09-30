---
layout: index
ref: index
lang: English
title: Gambarumeter
---

<div class="language-block">
        {% assign pages=site.pages | where:"ref", page.ref | sort: 'lang' %}
        {% for page in pages %}
          <span class="language">
            <a href="{{ site.baseurl }}{{ page.url }}" class="{{ page.lang }}">{{ page.lang }}</a>
          </span>
        {% endfor %}
</div>

Gambarumeter is an Android application that tracks your heart rates, running distance and stepcount.

Smartphones with Android 4.3 or newer and smartwatch with Android Wear are required to install this app.

This app works standalone. It's not necessary to carry smartphones.

## Contents:

- [Watch](#Watch)
- [Phone](#Phone)

## Watch<a name="Watch"></a>

### Main view

![Main view](screenshots/watch-main.png)
Main view is shown when launched.

If you tap the button at the bottom of the screen or double-tap somewhere on the screen, the app starts tracking your activity.
The app stops tracking if you tap the same button or double-tap somewhere.

You have to turn on sensors before launching this app.
Go to *Settings -> Permissions -> Gambarumeter*.

Distance is shown "-.--" at first. It turns to "0.00" when the device acquired satellite signals.

Long press somewhere on the screen if you want to terminate this app.
![Detail view](screenshots/watch-quit.png)

### History view

![History view](screenshots/watch-history-view.png)
You can view your workout history by swiping left.

History view has view mode and edit mode.

In view mode, tap a list item to see the detail of it.
![Detail view](screenshots/watch-detail.png)

In edit mode, tap a list item to delete it.
![History view](screenshots/watch-history-edit.png)

Mode is changed by tapping "View mode" or "Edit mode".


### Notification

Ongoing notification appears on the watch face. Tap it to open main view.


### Voice command

Voice commands are available if your smartwatch is connected with your smartphone.

"Start running" or "Start workout" to start the app.

Don't say "Stop running" or "Stop workout". Your workout data will be cleared.

## Phone<a name="Phone"></a>

### History view
![History view](screenshots/phone-history.png)
Main view of the smartphone app shows your workout history.

Tap one of them to see the detail of it.
Long-tap to delete it.

![Drawer/Main](screenshots/phone-drawer-main.png)

### Detail view

You can see map, chart and split time of your running.

Open drawer to move between them.

You can export data as tcx file.

![Detail/Map](screenshots/phone-map.png)
![Detail/Chart](screenshots/phone-chart.png)
![Detail/Splittime](screenshots/phone-splittime.png)

![Drawer/Detail](screenshots/phone-drawer-detail.png)
