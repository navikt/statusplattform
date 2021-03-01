import Head from 'next/head'
import Panel from 'nav-frontend-paneler';
// import Lenkepanel from 'nav-frontend-lenkepanel';

import '../styles/Home.css'
import FetchNavDigitalServices from './FetchNavDigitalServices'


async function fetchData() {
    console.log("fetch")
    const response = await fetch("http://localhost:3001/rest/v0.1/testAreas");
    const data = await response.json()
    return data
}

export default function Home() {
    // List of apis that are loaded in index. Should be sent as props to children components.
    let apisToMonitor;



    return (
        <div className="container">
            <Head>
                <title>Status digitale tjenester</title>
                <link rel="icon" href="/favicon.ico" />
                <meta name="viewport" content="initial-scale=1.0, width=device-width" />
            </Head>

            <main>
                <h1 className="title-container">
                    <img src="/assets/nav-logo/png/red.png" alt="LogoRed" />
                    Status digitale tjenester
                </h1>
                <div className="grid">
                    <FetchNavDigitalServices />

                    {/* <div
                        className="card"
                    >
                        <h3>Examples</h3>
                        <p>Discover and deploy boilerplate example Next.js projects. {fetchData}</p>
                    </div>

                    <a
                        className="card"
                    >
                        <h3>Arbeid</h3>
                        <p>
                            Api #1
					    </p>
                    </a>
                    <Panel border> asd </Panel> */}
                </div>
            </main>

            <footer className="footer">
                <img src="/assets/nav-logo/png/black.png" alt="LogoBlack" />
                <ul>
                    <li>Arbeids- og velferdsetaten</li>
                    <li>Personvern og informasjonskapsler</li>
                    <li>Tilgjengelighet</li>
                    <li>Del skjerm med veileder</li>
                </ul>
            </footer>
        </div>
    )
}
