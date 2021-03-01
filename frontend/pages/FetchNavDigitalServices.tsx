import { useEffect, useState } from "react";
import Panel from 'nav-frontend-paneler';



async function fetchData() {
    console.log("fetch")
    const response = await fetch("http://localhost:3001/rest/testAreas");
    const data = await response.json()
    console.log(data)
    // console.log(data.status)
    return data
}

function FetchNavDigitalServices() {
    const [areas, setAreas] = useState([])
    const [isLoading, setIsLoading] = useState(false)

    useEffect(() => {
        (async function () {
            setIsLoading(true)
            const newAreas = await fetchData()
            setAreas(newAreas)
            setIsLoading(false)
        })()
    }, [])

    if (isLoading) {
        return <p>Loading services...</p>
    }

    return (
        <div className="digital-services-container">
            {areas.map(area => (
                <Panel key={area.name}>
                    <ul>
                        {area.services.map(service => (
                            <li key={service.name} > {service.name}: {service.status}</li>
                        ))}
                    </ul>
                </Panel>
            ))}
        </div>

    )
}
{/* <ul>{areas.map((area) => <li key={area.name}>{area.name}: {area.status} -- {area.services.status}</li>)}</ul> */ }

export default FetchNavDigitalServices