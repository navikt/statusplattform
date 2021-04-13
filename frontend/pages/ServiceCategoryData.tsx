import { FC } from 'react'
import Layout from '../components/Layout/Layout'

export interface PortalAreaCategory {
    area: any
}

const ServiceCategoryData: FC<PortalAreaCategory> = (area) => {
    let test: boolean = true
    return (
        <Layout>
            {console.log(area)}
            {test
                ? "Ikke implementert enda"
                : <div>real</div>
            }
        </Layout>
    )
}

export default ServiceCategoryData