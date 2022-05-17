import React from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  description: JSX.Element;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'Easy and Free',
    description: (
      <>
        WallPanel allows any Android 4.4+ or FireOS tablet to display web-based
        dashboards. It is free and open-source. Use it for Home Assistant
        dashboards, security camera monitoring, or anything else!
      </>
    ),
  },
  {
    title: 'Remote Control',
    description: (
      <>
        WallPanel can be controlled via MQTT or HTTP, allowing you to remotely
        navigate to other web pages, speak (Text to Speech), play audio, change
        the brightness, and more using MQTT. Plus, WallPanel publishes sensors
        to MQTT so you can easily track the tablet's battery life, temperature,
        and more.
      </>
    ),
  },
  {
    title: 'Video streaming and motion detection',
    description: (
      <>
        Streaming MJPEG server support using the device camera. Camera support
        for motion detection, face detection, and QR Code reading. Screensaver
        feature that can be dismissed with motion or face detection.
      </>
    ),
  },
];

function Feature({title, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center padding-horiz--md">
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): JSX.Element {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
