import Head from 'next/head'
import '../styles/Home.css'
import Panel from 'nav-frontend-paneler';

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
                <h1 className="title">
                    Status digitale tjenester
                </h1>
                <div className="grid">
                    <div
                        className="card"
                    >
                        <h3>Examples</h3>
                        <p>Discover and deploy boilerplate example Next.js projects.</p>
                    </div>

                    <a
                        className="card"
                    >
                        <h3>Arbeid</h3>
                        <p>
                            Api #1
					    </p>
                    </a>
                    <Panel> sad</Panel>
                </div>
            </main>

            <footer className="footer">
                <img src="/assets/nav-logo/png/black.png" alt="Logo" />
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
