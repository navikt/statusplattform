import { useEffect, useState } from "react";
import Panel from 'nav-frontend-paneler';
import styled from 'styled-components'


const DigitalServicesContainer = styled.div`
    display: flex;
    flex: 1;
    flex-flow: row wrap;
    justify-content: center;
    width: 90%;
    white-space: normal;
`;

const PanelSetWidth = styled(Panel)`
    width: 250px;
    margin: 10px;
`;

async function fetchData() {
    // console.log("fetch")
    const response = await fetch("http://localhost:3001/rest/testAreas");
    const data = await response.json()
    // console.log(data)
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
        <DigitalServicesContainer>
            {areas.map(area => (
                <PanelSetWidth border key={area.name}>
                    <h3>{area.name}</h3>
                    <ul>
                        {area.services.map(service => (
                            <li key={service.name} > {service.name}: {service.status}</li>
                        ))}
                    </ul>
                </PanelSetWidth>
            ))}
        </DigitalServicesContainer>

    )
}
{/* <ul>{areas.map((area) => <li key={area.name}>{area.name}: {area.status} -- {area.services.status}</li>)}</ul> */ }

export default FetchNavDigitalServices