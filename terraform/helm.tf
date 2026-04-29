resource "helm_release" "ingress_nginx" {
  name             = "ingress-nginx"
  repository       = "https://kubernetes.github.io/ingress-nginx"
  chart            = "ingress-nginx"
  version          = "4.11.3"
  namespace        = "ingress-nginx"
  create_namespace = true

  set {
    name  = "controller.service.type"
    value = "LoadBalancer"
  }

  depends_on = [module.eks]
}

resource "helm_release" "kube_prometheus_stack" {
  name             = "kube-prometheus-stack"
  repository       = "https://prometheus-community.github.io/helm-charts"
  chart            = "kube-prometheus-stack"
  version          = "65.1.1"
  namespace        = "monitoring"
  create_namespace = true

  # Allow Prometheus to discover ServiceMonitors in any namespace
  set {
    name  = "prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues"
    value = "false"
  }

  # Allow Grafana to load dashboards from ConfigMaps labeled grafana_dashboard=1
  set {
    name  = "grafana.sidecar.dashboards.enabled"
    value = "true"
  }

  set {
    name  = "grafana.sidecar.dashboards.searchNamespace"
    value = "ALL"
  }

  depends_on = [module.eks]
}

resource "helm_release" "dev_platform" {
  name             = "dev-platform"
  chart            = "${path.module}/../helm/dev-platform"
  namespace        = "dev-platform"
  create_namespace = true

  set {
    name  = "backend.image.tag"
    value = var.app_image_tag
  }

  set {
    name  = "frontend.image.tag"
    value = var.app_image_tag
  }

  set {
    name  = "ingress.host"
    value = var.ingress_host
  }

  set_sensitive {
    name  = "postgres.password"
    value = var.db_password
  }

  depends_on = [
    helm_release.ingress_nginx,
    helm_release.kube_prometheus_stack,
  ]
}
