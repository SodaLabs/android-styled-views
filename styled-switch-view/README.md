### Gradle Integration

[![CircleCI](https://circleci.com/gh/SodaLabs/styled-views-android.svg?style=svg)](https://circleci.com/gh/SodaLabs/styled-views-android)
[ ![Download](https://api.bintray.com/packages/sodalabs/android/styled-switch-view/images/download.svg) ](https://bintray.com/sodalabs/android/styled-switch-view/_latestVersion)

Add this into your dependencies block.

```
implementation 'co.sodalabs:styled-switch-view:x.x.x'
```

If you cannot find the package on JCenter, add this to your gradle repository

```
maven {
    url 'https://dl.bintray.com/sodalabs/android'
}
```

### Usage

The capsule switch has a minimum size of **72 by 32 dip** and always follows the aspect ratio of 72/32. Here are some sizing cases:

- Given `android:layout_width="wrap_content"` and `android:layout_height="wrap_content"`, it is measured as minimum size.
- Given other settings, it shrinks down to fit the scale size.

##### Styled attributes

- `app:swOn`: Switch on or off.
- `app:swTextOn`: The label for on state.
- `app:swTextOff`: The label for off state.
- `app:swTextColorOn`: The label color for on state.
- `app:swTextColorOff`: The label color for off state.
- `app:swTextSize`: The text size measured in **sp**.
- `app:swBackgroundColorOn`: The background color for on state.
- `app:swBackgroundColorOff`: The background color for off state.
- `app:swThumbColorOn`: The thumbnail color for on state.
- `app:swThumbColorOff`: The thumbnail color for off state.
- `app:swBorderColorOn`: The border color for on state.
- `app:swBorderColorOff`: The border color for off state.
- `app:swBorderWidth`: The border width measured in **dp**.
- `app:swColorDisabled`: The general color when it is disabled.

##### Preview

<p align="center">
  <img src="../docs/sw-capsule-demo.gif" width="240">
</p>

##### XML Sample

```XML
<co.sodalabs.view.StyledSwitchView
    ...
    app:swBackgroundColorOff="#00415C"
    app:swBackgroundColorOn="#33677C"
    app:swBorderColorOff="#33677C"
    app:swBorderColorOn="#33677C"
    app:swBorderWidth="1dp"
    app:swColorDisabled="#33677C"
    app:swOn="true"
    app:swTextColorOff="#D6E0E4"
    app:swTextColorOn="#D6E0E4"
    app:swThumbColorOff="#D6E0E4"
    app:swThumbColorOn="#D6E0E4" />
```


